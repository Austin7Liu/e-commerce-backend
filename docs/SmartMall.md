# SmartMall 项目技术文档

## 1. 项目定位

SmartMall 是一个基于 Spring Boot 的电商后端项目。目前处于基础业务建设阶段，已经完成商品与商品分类两个核心模块，后续计划逐步加入用户、购物车、订单、库存以及智能购物助手等能力。

本文档用于记录项目中已经实际落地的重要技术方案、业务规则和设计取舍，既作为后续开发依据，也用于面试时说明项目实现。文档只描述当前已经实现的能力；尚未实现的规划会明确标注，避免把设计设想描述成现有功能。

## 2. 技术栈

| 技术 | 当前版本或用途 |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Maven | 项目构建和依赖管理 |
| Spring Web MVC | REST API |
| MySQL | 8.0.26，业务数据存储 |
| MyBatis-Plus | ORM 映射、CRUD、条件查询和分页 |
| Lombok | 简化实体类 Getter、Setter |
| JUnit 5、MockMvc | Service、Mapper 和 Controller 测试 |

MyBatis-Plus 使用适配 Spring Boot 4 的 Starter：

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
    <version>3.5.15</version>
</dependency>
```

项目通过 MySQL Connector/J 连接 MySQL。数据库连接配置目前位于 `application.properties`。

## 3. 项目架构

项目遵循下面的单向调用关系：

```text
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Mapper
    ↓
MySQL
```

各层职责如下：

- Controller：接收 HTTP 参数和请求体，完成 DTO 与业务对象转换，选择合适的 HTTP 状态码。
- Service：执行参数校验、分类关系校验、删除约束等业务逻辑。
- Mapper：仅负责数据库访问，当前 Mapper 均继承 MyBatis-Plus `BaseMapper`。
- Entity：映射数据库表，不直接作为 API 响应返回。
- DTO：隔离数据库实体与外部 API，避免暴露 `deleted` 等内部字段。

当前主要包结构：

```text
com.aicode.smartmall
├── category
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── exception
│   ├── mapper
│   └── service
├── config
└── product
    ├── controller
    ├── dto
    ├── entity
    ├── mapper
    └── service
```

## 4. Product 商品模块

### 4.1 数据模型

商品表当前核心字段如下：

| 字段 | Java 类型 | 说明 |
|---|---|---|
| `id` | `Long` | 自增主键，使用无符号 BIGINT |
| `category_id` | `Long` | 所属分类，非空并关联分类表 |
| `name` | `String` | 商品名称，最大 200 字符 |
| `main_image_url` | `String` | 商品主图地址，可空 |
| `price` | `BigDecimal` | 销售价格，对应 `DECIMAL(10,2)` |
| `stock` | `Long` | 库存，对应 `INT UNSIGNED` |
| `description` | `String` | 商品描述，可空 |
| `status` | `Integer` | 0 表示下架，1 表示上架 |
| `deleted` | `Integer` | 逻辑删除标记 |
| `created_time` | `LocalDateTime` | 创建时间 |
| `updated_time` | `LocalDateTime` | 更新时间 |

价格使用 `BigDecimal`，而不是 `double`，原因是二进制浮点数无法精确表示多数十进制小数，直接用于金额计算可能产生精度误差。

库存数据库类型是 `INT UNSIGNED`，最大值为 4294967295，超过 Java `Integer` 的最大值，因此实体中使用 `Long`。

`main_image_url` 和 `description` 允许为空。这样可以保存尚未完善的下架商品；API 响应中对应字段可能为 `null`。

### 4.2 主键与分类关系

商品使用自增 BIGINT 主键。当前系统是单体应用和单数据库，自增主键简单、稳定，能够满足按 ID 查询和排序需求，暂时没有引入分布式 ID 生成器。

Product 与 Category 是多对一关系：

```text
多个 Product → 一个 Category
```

数据库通过 `product.category_id` 外键关联 `category.id`。当前业务标准规定所有商品必须属于一个分类，因此 `category_id` 已设置为 `NOT NULL`。

商品创建和更新不会只依赖数据库外键。Service 会先检查：

1. `categoryId` 必须提供且为正数。
2. 分类必须存在并且没有被逻辑删除。
3. 商品上架时，所属分类及其全部祖先分类都必须启用。
4. 分类层级如果因异常数据形成循环，会拒绝商品保存，避免无限遍历。

数据库外键只能判断分类物理记录是否存在，不能识别分类是否已经逻辑删除，因此业务有效性检查必须放在 Service 层。

### 4.3 商品基础操作

当前 Product Service 支持：

- 按 ID 查询商品。
- 新增商品。
- 按 ID 部分更新商品。
- 按 ID 逻辑删除商品。
- 分页查询商品。
- 按商品状态筛选。
- 按商品名称进行简单模糊查询。

对应 API：

| 方法 | 地址 | 说明 |
|---|---|---|
| GET | `/api/products/{id}` | 商品详情 |
| GET | `/api/products` | 商品分页列表 |
| POST | `/api/products` | 新增商品 |
| PATCH | `/api/products/{id}` | 部分更新商品 |
| DELETE | `/api/products/{id}` | 逻辑删除商品 |

### 4.4 商品分页查询

商品列表支持：

```text
page     页码，默认 1
size     每页数量，默认 20，范围 1～100
status   可选，0 或 1
keyword  可选，商品名称模糊匹配
```

示例：

```http
GET /api/products?page=1&size=10&status=1&keyword=音箱
```

实现使用 MyBatis-Plus：

```java
Page<Product>
LambdaQueryWrapper<Product>
productMapper.selectPage(...)
```

条件通过 `eq(condition, ...)` 和 `like(condition, ...)` 动态拼接，不需要手写 Mapper XML。查询结果按 `id DESC` 排序，使新商品优先显示。

## 5. Category 商品分类模块

### 5.1 为什么采用树形分类

分类采用父子结构：

```text
电子产品
├── 智能穿戴
└── 音频设备

箱包
└── 双肩包
```

`category.parent_id` 指向同表的 `category.id`，一级分类的 `parent_id` 为 `NULL`。这种邻接表设计简单直观，适合当前分类层级较浅、主要按父节点查询直属子分类的场景。

目前没有引入闭包表、路径字段或嵌套集合。那些方案更适合频繁查询任意深度整棵树的场景，但会增加写入和维护复杂度，不符合当前阶段的实际需求。

### 5.2 分类数据模型

| 字段 | Java 类型 | 说明 |
|---|---|---|
| `id` | `Long` | 分类自增主键 |
| `parent_id` | `Long` | 父分类 ID，一级分类为 NULL |
| `name` | `String` | 分类名称，最大 100 字符 |
| `sort_order` | `Integer` | 同级排序值，越小越靠前 |
| `status` | `Integer` | 0 表示禁用，1 表示启用 |
| `deleted` | `Integer` | 逻辑删除标记 |
| `created_time` | `LocalDateTime` | 创建时间 |
| `updated_time` | `LocalDateTime` | 更新时间 |

分类表存在自关联外键：

```text
category.parent_id → category.id
```

外键使用 `ON DELETE RESTRICT`，不允许物理删除仍有子分类的父分类。但系统实际采用逻辑删除，所以删除业务仍然需要由 Service 主动检查。

### 5.3 分类基础操作

当前支持：

- 新增一级或子分类。
- 按 ID 查询分类。
- 查询一级分类或某个分类的直属子分类。
- 分页查询一级分类或直属子分类。
- 按状态筛选分类。
- 按名称模糊查询分类。
- 修改分类名称、排序、状态和父分类。
- 对空分类执行逻辑删除。

对应 API：

| 方法 | 地址 | 说明 |
|---|---|---|
| GET | `/api/categories/{id}` | 分类详情 |
| GET | `/api/categories` | 一级分类或直属子分类列表 |
| GET | `/api/categories/page` | 分类分页查询 |
| POST | `/api/categories` | 新增分类 |
| PATCH | `/api/categories/{id}` | 部分更新分类 |
| DELETE | `/api/categories/{id}` | 删除空分类 |

### 5.4 分类列表和分页

不分页接口适合分类选择器和逐级加载：

```http
GET /api/categories
GET /api/categories?parentId=54&status=1
```

分页接口适合后台管理：

```http
GET /api/categories/page?page=1&size=10
GET /api/categories/page?page=1&size=10&parentId=54
GET /api/categories/page?page=1&size=10&parentId=54&status=1&name=智能
```

分页参数：

```text
page      默认 1，必须大于等于 1
size      默认 20，范围 1～100
parentId  可选；不传时查询一级分类
status    可选；0 表示禁用，1 表示启用
name      可选；去除首尾空格后进行名称模糊查询
```

分页响应：

```json
{
  "categories": [],
  "total": 0,
  "page": 1,
  "size": 10,
  "totalPages": 0
}
```

查询使用 MyBatis-Plus `Page`、`LambdaQueryWrapper` 和 `selectPage`。分类按照 `sort_order ASC, id ASC` 排序，从而保证同级分类顺序稳定。

### 5.5 父子循环校验

分类修改父节点时必须避免形成循环。例如：

```text
电子产品
└── 智能穿戴
```

如果把“电子产品”的父分类改成“智能穿戴”，就会形成闭环。

当前实现从目标父分类开始，逐级查询其祖先：

```text
目标父分类 → 父分类的父分类 → 更上层分类
```

如果过程中遇到当前分类 ID，立即拒绝更新；同时使用 `Set<Long>` 记录已经访问的 ID，防止数据库中已有异常循环时出现无限循环。

分类通常只有少量层级，因此逐级调用 `selectById` 足以满足当前需求，不需要为了这一操作引入递归 SQL。

### 5.6 分类逻辑删除业务

分类和商品都使用 MyBatis-Plus `@TableLogic`：

```java
@TableLogic(value = "0", delval = "1")
private Integer deleted;
```

调用 `deleteById` 时，实际执行的是类似下面的更新：

```sql
UPDATE category
SET deleted = 1
WHERE id = ?
  AND deleted = 0;
```

MyBatis-Plus 在普通查询中自动加入未删除条件。Service 的子分类数量和商品数量查询也会自动排除已经逻辑删除的记录。

#### 为什么删除分类时不能直接级联

分类可能同时拥有子分类和关联商品。如果删除父分类时自动级联删除，会让整棵分类树和大量商品同时不可见，误操作影响过大，恢复过程也复杂。

如果自动把子分类移动到根节点、把商品分类置空，也会悄悄改变业务含义。并且当前 `product.category_id` 为非空，商品本身不允许处于无分类状态。

因此第一版采用限制删除策略：

1. 查询分类是否存在且未删除。
2. 统计该分类下未删除的直属子分类。
3. 统计绑定该分类的未删除商品。
4. 任意数量大于 0 时拒绝删除。
5. 只有没有子分类、没有商品的空分类才能逻辑删除。

存在关联数据时返回：

```http
409 Conflict
```

响应中包括：

```json
{
  "message": "Category cannot be deleted because it has 1 child categories and 2 associated products",
  "childCount": 1,
  "productCount": 2
}
```

如果分类只是暂时不再展示，应把 `status` 改为 0，而不是删除。真正删除前，应先明确迁移子分类和商品；未来如果实现迁移功能，应由请求显式指定目标分类，不能在 DELETE 接口中隐式选择目标。

数据库外键无法替代这段逻辑，因为逻辑删除只是修改 `deleted` 字段，分类物理记录仍存在，外键不会阻止该操作。

## 6. 索引设计

当前 Product 的主要访问方式是按主键查询、分页列表和按分类查询。Category 的主要访问方式是按主键查询以及按父分类加载直属子分类。

已有分类索引：

```sql
idx_category_parent_list(parent_id, deleted, sort_order, id)
```

它服务于以下真实查询：

```sql
WHERE parent_id = ?
  AND deleted = 0
ORDER BY sort_order, id
```

已有商品分类索引：

```sql
idx_product_category_list(category_id, deleted, status, id)
```

它用于按分类查询未删除商品，并可以继续按商品状态筛选。

名称查询当前使用 `%关键字%`。普通 B-Tree 索引不能有效优化前导百分号模糊查询，而且当前商品和分类数据量较小，因此没有为名称提前增加无效索引，也没有引入 Elasticsearch。

## 7. HTTP 状态码约定

| 状态码 | 使用场景 |
|---|---|
| 200 | 查询或更新成功 |
| 201 | 创建成功，并返回 `Location` 响应头 |
| 204 | 删除成功，无响应体 |
| 400 | 页码、状态、名称等参数不合法，或分类关系形成循环 |
| 404 | 商品或分类不存在，或已被逻辑删除 |
| 409 | 分类仍有子分类或关联商品，不能删除 |

## 8. 测试策略

项目当前使用真实 Spring Boot 上下文和 MySQL 进行集成测试，并通过 `@Transactional` 在每个测试结束后回滚测试数据。

测试覆盖：

- Entity 与数据库字段映射。
- MyBatis-Plus `BaseMapper` 基础操作。
- 商品创建、查询、更新和逻辑删除。
- 商品分页、状态筛选和名称模糊查询。
- 分类创建、查询、排序、更新和逻辑删除。
- 分类分页、状态筛选和名称模糊查询。
- 分类父子循环校验。
- 有子分类或商品时拒绝删除分类。
- 商品分类必填及上架分类启用校验。
- Controller HTTP 状态码和响应结构。

截至分类分页功能完成时，完整测试共 26 个，全部通过。

## 9. 当前设计边界

当前版本刻意保持简单，尚未实现：

- 一次返回任意深度的完整分类树。
- 分类及其商品的批量迁移。
- 商品复杂全文搜索。
- 独立库存流水和库存扣减并发控制。
- 全局统一异常响应结构。
- 数据库版本迁移工具。

另外，当前 PATCH 请求使用简单 Record DTO，无法区分“JSON 中没有提供某个可空字段”和“显式把该字段设置为 null”。如果后续需要主动清空主图、描述或把分类移动为根分类，应增加能够记录字段是否出现的更新模型，而不是直接依赖 null。

分类删除目前采用事务完成检查和逻辑删除。在极高并发下，检查完成后仍可能出现新的子分类或商品绑定。当前阶段通过 Service 校验和数据库外键满足基本一致性；后续若出现真实并发管理场景，可再评估行锁或更严格的并发控制，不提前增加复杂度。

## 10. 后续文档维护规则

以后每完成一个业务功能，应同步更新本文档，至少记录：

1. 功能解决的业务问题。
2. Controller、Service、Mapper 的调用关系。
3. 关键数据模型和约束。
4. 重要业务校验与异常处理。
5. 使用到的 MyBatis-Plus 或 Spring 能力。
6. 索引设计及其对应的真实查询。
7. 测试覆盖和验证结果。
8. 当前方案的取舍、限制和可演进方向。

文档应以实际代码和数据库现状为准，不记录尚未实现的功能为既有能力。

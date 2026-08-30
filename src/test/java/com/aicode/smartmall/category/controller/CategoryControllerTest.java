package com.aicode.smartmall.category.controller;

import com.aicode.smartmall.category.entity.Category;
import com.aicode.smartmall.category.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

    @Test
    void shouldCreateQueryListUpdateAndDeleteCategory() throws Exception {
        String response = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Controller category",
                                  "sortOrder": 5,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Controller category"))
                .andReturn().getResponse().getContentAsString();

        Long id = Long.valueOf(response.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(get("/api/categories").param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")]").exists());

        mockMvc.perform(patch("/api/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated controller category"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated controller category"));

        mockMvc.perform(delete("/api/categories/{id}", id))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictWhenCategoryHasChild() throws Exception {
        Category parent = createCategory(null, "Controller conflict parent");
        createCategory(parent.getId(), "Controller conflict child");

        mockMvc.perform(delete("/api/categories/{id}", parent.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.childCount").value(1))
                .andExpect(jsonPath("$.productCount").value(0));
    }

    @Test
    void shouldReturnCategoryPageFilteredByName() throws Exception {
        Category root = createCategory(null, "Controller page root");
        Category matching = createCategory(root.getId(), "Controller portable audio");
        createCategory(root.getId(), "Controller smart wearable");

        mockMvc.perform(get("/api/categories/page")
                        .param("page", "1")
                        .param("size", "10")
                        .param("parentId", root.getId().toString())
                        .param("status", "1")
                        .param("name", "portable audio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.categories.length()").value(1))
                .andExpect(jsonPath("$.categories[0].id").value(matching.getId()));
    }

    @Test
    void shouldReturnBadRequestForInvalidCategoryPageQuery() throws Exception {
        mockMvc.perform(get("/api/categories/page").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page size must be between 1 and 100"));
    }

    private Category createCategory(Long parentId, String name) {
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(name);
        category.setSortOrder(0);
        category.setStatus(1);
        return categoryService.create(category);
    }
}

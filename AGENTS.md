# SmartMall AI

## Project Overview

SmartMall AI is a Spring Boot based intelligent e-commerce backend system.

The project will gradually include:
- user management
- product management
- shopping cart
- order management
- inventory management
- AI shopping assistant
- AI customer service

## Tech Stack

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring Web
- MySQL
- Lombok
- MyBatis-Plus

Do not introduce new frameworks, databases, message queues, caching systems, or AI dependencies unless explicitly requested.
For the current stage, focus on basic Spring Boot e-commerce features.
Do not introduce AI-related dependencies unless explicitly requested.

## Architecture

Use the following dependency direction:

Controller
-> Service
-> Mapper
-> Database

Rules:

- Controller is responsible for receiving HTTP requests and returning responses.
- Business logic belongs in the Service layer.
- Mapper is responsible only for database access.
- Controller must not call Mapper directly.
- DTOs should be used for request and response objects when appropriate.
- Entity objects should not be returned directly from APIs when avoidable.

## Coding Rules

- Follow the existing project naming and package conventions.
- Prefer simple, readable, and maintainable code.
- Do not over-engineer.
- Do not introduce unnecessary abstractions or design patterns.
- Do not add new dependencies unless explicitly requested.
- Do not modify unrelated files.
- Validate external input when necessary.
- Keep business logic out of Controller classes.
- Preserve existing behavior unless the task explicitly requires a change.

## Testing Rules

- Run existing tests after modifying business logic.
- Add or update tests when necessary.
- Do not delete or disable tests just to make the build pass.
- If a test fails, investigate the root cause before making additional changes.
- Prefer focused tests for the changed behavior.
- Report which tests were executed and whether they passed.

## Agent Workflow

For non-trivial tasks, follow this workflow:

1. Explore the relevant code and files first.
2. Understand the existing implementation and project conventions.
3. Explain the implementation plan before making changes.
4. Identify the files that need to be modified.
5. Implement the smallest reasonable change.
6. Run relevant tests or verification commands.
7. Review the final diff.
8. Report:
    - what was changed
    - why it was changed
    - what tests or verification were performed
    - whether any risks or unresolved issues remain
9. Complete the SmallMall.md located in the "docs" folder under the root directory.
Do not make large or unrelated changes without explaining them first.
# Recipe Book API

[![CI](https://github.com/sanimanqezu/recipe-book-api/actions/workflows/ci.yml/badge.svg)](https://github.com/sanimanqezu/recipe-book-api/actions/workflows/ci.yml)

A RESTful API for managing recipes, nested ingredients, and quick meal-plan suggestions — built as a portfolio project on Spring Boot 3.

## Tech Stack

| Technology        | Version  | Purpose                          |
|-------------------|----------|----------------------------------|
| Java              | 25       | Language                         |
| Spring Boot       | 3.5.16   | Application framework            |
| Spring Data JPA   | —        | ORM / database access            |
| PostgreSQL        | 16       | Shared portfolio database        |
| Flyway            | —        | Database schema migrations       |
| Lombok            | —        | Boilerplate reduction            |
| Spring Actuator   | —        | Health / info endpoints          |
| JUnit 5 / Mockito | —        | Service unit & controller slice tests |
| H2                | —        | In-memory database for tests     |
| Docker            | —        | Containerisation                 |

## Key design points

- **Layered architecture.** Controllers stay thin and delegate to `RecipeService`; the service owns all business logic and transaction boundaries (`@Transactional(readOnly = true)` by default, write methods opt in).
- **Records as DTOs.** `RecipeRequest`, `RecipeResponse`, and `IngredientDto` are immutable records; Jakarta Bean Validation (`@NotBlank`, `@NotNull`, `@Min`, `@Valid`) is declared on the request record and enforced at the controller boundary.
- **Aggregate-managed ingredients.** A recipe owns its ingredients. `Recipe` exposes `addIngredient` / `clearIngredients` helpers that keep both sides of the relationship consistent, so an update fully replaces the ingredient set.
- **Centralised error handling.** A single `@RestControllerAdvice` maps `ResourceNotFoundException` to 404 and validation failures to 400 with a structured body (timestamp, status, error, field errors).
- **Schema owned by Flyway.** `ddl-auto` is `validate`; the schema is created and versioned by migration scripts, not by Hibernate.
- **Environment-driven configuration.** Datasource host, port, name, and credentials come from environment variables with sensible local defaults.

## Project Structure

```
recipe-book-api/
├── src/
│   ├── main/
│   │   ├── java/com/sanele/recipebook/
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Request/Response records
│   │   │   ├── entity/           # JPA entities & enums
│   │   │   ├── exception/        # ResourceNotFoundException + global handler
│   │   │   ├── repository/       # Spring Data repositories
│   │   │   └── service/          # Business logic
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/     # Flyway SQL scripts
│   └── test/
│       └── java/com/sanele/recipebook/
│           ├── controller/       # @WebMvcTest slice tests
│           └── service/          # Mockito unit tests
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Running locally

This service connects to the **shared portfolio PostgreSQL** instance rather than bundling its own. Start that first from the portfolio root, then run the app.

**Prerequisites:** Java 25, Maven 3.9+, Docker.

```bash
# 1. From the portfolio root, start the shared Postgres (and the portfolio-net network)
docker compose up -d

# 2. Configure the datasource (defaults shown; override as needed)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=recipebook
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# 3. Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`, with health at `http://localhost:8080/actuator/health`.

### Running the container

The provided `docker-compose.yml` builds the app image and attaches it to the external `portfolio-net` network so it can reach the shared Postgres:

```bash
docker compose up --build -d
```

## Tests

```bash
./mvnw test
```

Tests are self-contained — no real database or network is required.

- **Service unit tests** (`RecipeServiceTest`) — `@ExtendWith(MockitoExtension.class)` with a mocked `RecipeRepository`, covering create (with and without ingredients), read, filtered listing, update (found / not-found), delete (found / not-found), ingredient search, and meal-plan generation.
- **Controller slice tests** (`RecipeControllerTest`) — `@WebMvcTest` with a `@MockitoBean` service, asserting status codes, request delegation, validation failures mapping to 400, and `ResourceNotFoundException` mapping to 404 via the global handler.

Current suite: **28 tests** (15 service + 13 controller).

## API Endpoints

Base path: `/api/recipes`

| Method | Endpoint                    | Description                                           |
|--------|-----------------------------|-------------------------------------------------------|
| POST   | `/api/recipes`              | Create a new recipe with nested ingredients           |
| GET    | `/api/recipes`              | List all recipes (optional `?cuisine=` `&category=`) |
| GET    | `/api/recipes/{id}`         | Get a recipe by ID                                    |
| PUT    | `/api/recipes/{id}`         | Update a recipe by ID                                 |
| DELETE | `/api/recipes/{id}`         | Delete a recipe by ID                                 |
| GET    | `/api/recipes/search`       | Search recipes by ingredient name `?ingredient=`      |
| GET    | `/api/recipes/meal-plan`    | Get up to 3 random recipes as a meal plan suggestion  |

### Supported Enum Values

**Cuisine:** `ITALIAN`, `AFRICAN`, `ASIAN`, `AMERICAN`, `OTHER`

**Category:** `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK`, `DESSERT`

## Example Requests

### Create a Recipe

```bash
curl -X POST http://localhost:8080/api/recipes \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Jollof Rice",
    "description": "A classic West African one-pot rice dish.",
    "cuisine": "AFRICAN",
    "category": "DINNER",
    "prepTimeMinutes": 20,
    "cookTimeMinutes": 45,
    "servings": 6,
    "instructions": "Blend tomatoes, fry in oil, add rice and stock, simmer until cooked.",
    "ingredients": [
      { "name": "long grain rice", "quantity": "500", "unit": "g" },
      { "name": "tomatoes", "quantity": "4", "unit": "whole" },
      { "name": "chicken stock", "quantity": "750", "unit": "ml" }
    ]
  }'
```

### List Recipes with Filters

```bash
# All recipes
curl http://localhost:8080/api/recipes

# Filter by cuisine
curl "http://localhost:8080/api/recipes?cuisine=AFRICAN"

# Filter by cuisine and category
curl "http://localhost:8080/api/recipes?cuisine=AFRICAN&category=DINNER"
```

### Search by Ingredient

```bash
curl "http://localhost:8080/api/recipes/search?ingredient=rice"
```

### Meal Plan

```bash
curl http://localhost:8080/api/recipes/meal-plan
```

## Error Responses

| Status | Scenario                            |
|--------|-------------------------------------|
| 400    | Validation failure (blank title, etc.) |
| 404    | Recipe not found by ID              |
| 500    | Unexpected server error             |

Error responses follow this structure:

```json
{
  "timestamp": "2026-04-11T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Recipe not found with id: 99"
}
```

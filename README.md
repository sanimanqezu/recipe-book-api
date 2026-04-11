# Recipe Book API

A RESTful API for managing recipes, built as a portfolio project demonstrating Spring Boot 3 best practices.

## Tech Stack

| Technology        | Version  | Purpose                          |
|-------------------|----------|----------------------------------|
| Java              | 17       | Language                         |
| Spring Boot       | 3.2.5    | Application framework            |
| Spring Data JPA   | —        | ORM / database access            |
| PostgreSQL        | 16       | Production database               |
| H2                | —        | In-memory database for tests     |
| Flyway            | —        | Database schema migrations       |
| Lombok            | —        | Boilerplate reduction            |
| JUnit 5 / Mockito | —        | Unit testing                     |
| Docker            | —        | Containerisation                 |

## Project Structure

```
recipe-book-api/
├── src/
│   ├── main/
│   │   ├── java/com/sanele/recipebook/
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Request/Response records
│   │   │   ├── entity/           # JPA entities & enums
│   │   │   ├── exception/        # Global error handling
│   │   │   ├── repository/       # Spring Data repositories
│   │   │   └── service/          # Business logic
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/     # Flyway SQL scripts
│   └── test/
│       └── java/com/sanele/recipebook/
│           └── service/          # Unit tests
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Running with Docker

**Prerequisites:** Docker and Docker Compose installed.

```bash
# Clone the repository
git clone https://github.com/sanele/recipe-book-api.git
cd recipe-book-api

# Build and start all services (app + PostgreSQL)
docker compose up --build

# Run in detached mode
docker compose up --build -d

# Stop all services
docker compose down

# Stop and remove volumes (wipes database)
docker compose down -v
```

The API will be available at `http://localhost:8080`.

## Running Locally (without Docker)

**Prerequisites:** Java 17, Maven 3.9+, and a running PostgreSQL instance.

```bash
# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/recipebook
export DB_USER=postgres
export DB_PASSWORD=postgres

# Run the application
./mvnw spring-boot:run
```

## Running Tests

```bash
./mvnw test
```

Tests use an H2 in-memory database — no external dependencies required.

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
| GET    | `/api/recipes/meal-plan`    | Get 3 random recipes as a meal plan suggestion        |

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

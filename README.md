# GameReview API

This project is a Spring Boot REST API for games, users, and game reviews. It uses PostgreSQL in production and can use a local PostgreSQL container for development.

## Tech used

- Java 17
- Spring Boot
- Gradle
- PostgreSQL
- Docker and Docker Compose

## Run locally with Docker

You need Docker Desktop installed.

```bash
docker compose up --build
```

The API will be available at:

```text
http://localhost:8080
```

To stop the containers, run:

```bash
docker compose down
```

The local database uses these default credentials:

```text
username: cst438
password: cst438-local
database: cst438
```

## Run with Gradle

If you already have PostgreSQL running, set `SUPABASE_DB_URL` to a JDBC PostgreSQL URL and run:

```bash
./gradlew bootRun
```

To run the tests:

```bash
./gradlew test
```

## Authentication

Local development has basic authentication enabled by default.

```text
admin / admin-password
user / user-password
```

These are default development credentials only. Production credentials and database settings should be added as environment variables in Render or another hosting service.

## API links

- Local API: http://localhost:8080
- OpenAPI contract: [docs/openapi.yaml](docs/openapi.yaml)
- Production API: https://gamereview-api-gqbj.onrender.com
- Production Swagger UI: https://gamereview-api-gqbj.onrender.com/swagger-ui.html

## Main endpoints

- `GET /api/v1/games` lists games with pagination.
- `GET /api/v1/games/{gameId}` gets one game.
- `POST /api/v1/games` and `PATCH` or `DELETE /api/v1/games/{gameId}` are admin operations.
- `GET /api/v1/games/{gameId}/reviews` lists reviews for a game.
- `POST /api/v1/games/{gameId}/reviews` creates a review.
- `GET`, `PUT`, `PATCH`, and `DELETE /api/v1/reviews/{reviewId}` manage reviews.
- `GET /api/v1/users/me/reviews` lists the signed-in user's reviews.

More endpoint details are in the OpenAPI file in the `docs` folder.

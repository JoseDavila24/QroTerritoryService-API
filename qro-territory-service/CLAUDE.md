# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Development (hot reload, Dev UI at http://localhost:8080/q/dev/)
./mvnw quarkus:dev

# Run unit tests
./mvnw test

# Run unit + integration tests
./mvnw verify

# Run a single test class
./mvnw test -Dtest=ColoniasResourceTest

# Build standard JAR → target/quarkus-app/quarkus-run.jar
./mvnw package

# Build über-JAR (single executable)
./mvnw package -Dquarkus.package.jar.type=uber-jar

# Build native executable (requires GraalVM)
./mvnw package -Dnative
# Or via Docker (no local GraalVM needed):
./mvnw package -Dnative -Dquarkus.native.container-build=true

# Start full stack (MySQL + dev app) via Docker Compose
docker-compose up -d
```

## Architecture

**What it is:** A Quarkus 3 REST API that normalizes geographic/administrative data for the Municipality of Querétaro, Mexico. It bridges SEPOMEX postal codes with local political delegations, enabling delegation → colonias (neighborhoods) hierarchical selectors.

**Stack:** Java 21 · Quarkus 3.34.6 · MySQL 8.0 · Hibernate ORM Panache · Caffeine cache · Jakarta REST + Jackson · OpenAPI 3.0.4 generator

### Layers

```
com.qroterritory/
├── resource/
│   ├── ColoniasResource.java        # Public GET endpoints for colonies
│   ├── AdminColoniasResource.java   # Admin POST/PUT endpoints (requires X-API-KEY)
│   └── DelegacionesResource.java    # Public GET endpoint for delegations
├── entity/
│   ├── ColoniaEntity.java           # Active Record — colonias table
│   └── DelegacionEntity.java        # Active Record — delegaciones table
└── security/
    └── ApiKeyFilter.java            # ContainerRequestFilter for /admin/* routes
```

### Key Conventions

- **Active Record pattern:** entities extend `PanacheEntityBase` and query themselves via static methods (`find()`, `listAll()`, etc.)
- **DTO mapping:** resource classes map between JPA entities and OpenAPI-generated DTOs (generated from `src/main/openapi/openapi.yaml`); never expose entities directly
- **Enum conversion:** `TipoAsentamiento` stored as its `name()` string in MySQL, converted via `valueOf()` on read
- **Lazy loading:** `@ManyToOne(fetch = FetchType.LAZY)` on `ColoniaEntity.delegacion` to avoid N+1
- **Transactional writes:** `@Transactional` on all POST/PUT methods in admin resource
- **Cache invalidation:** `@CacheInvalidateAll` by cache name on every admin mutation

### Caching

Two Caffeine caches, both 10-minute TTL:
- `colonias-por-delegacion` — key: `delegacion_id:page:size`
- `lista-delegaciones` — no-arg, full list

### Security

`ApiKeyFilter` intercepts any URL containing `/admin`. It reads the `X-API-KEY` request header and compares it against the `admin.api.key` config property (default: `Qro-Secret-Key-2026`). Returns `401 Unauthorized` and aborts on mismatch.

### Database

`application.properties` connects to `jdbc:mysql://db:3306/qro_territory_db` (the `db` hostname resolves inside Docker Compose). `quarkus.hibernate-orm.database.generation=update` auto-creates/updates schema. `import.sql` seeds test data (3 delegations, 3 colonies) on every startup.

### API Surface

All routes are prefixed with `/api/v1` (`quarkus.http.root-path`). The full OpenAPI spec lives at `src/main/openapi/openapi.yaml`; Swagger UI is at `http://localhost:8080/q/swagger-ui`.

| Method | Path | Auth |
|--------|------|------|
| GET | `/delegaciones` | none |
| GET | `/colonias?delegacion_id=&page=&size=` | none |
| GET | `/colonias/{id}` | none |
| POST | `/admin/colonias` | X-API-KEY |
| PUT | `/admin/colonias/{id}` | X-API-KEY |

### Configuration

Override `application.properties` values via environment variables at runtime:
- `QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME`, `QUARKUS_DATASOURCE_PASSWORD`
- `ADMIN_API_KEY` (maps to `admin.api.key`)

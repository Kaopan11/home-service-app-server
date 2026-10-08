# HomeServices Server

> REST API for a home-repair marketplace: auth, service catalog, Omise checkout, admin catalog/promos, technician jobs, and in-app notifications.

[![Java](docs/badges/java.png)](https://openjdk.org/)
[![Spring Boot](docs/badges/spring.png)](https://spring.io/projects/spring-boot)
[![PostgreSQL](docs/badges/postgres.png)](https://www.postgresql.org/)
[![Docker](docs/badges/docker.png)](https://www.docker.com/)

This repo is the backend only. The Vue SPA lives in `home-service-app-client`.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Reference](#api-reference)
- [Database](#database)
- [Deployment](#deployment)
- [Related Repos](#related-repos)
- [Author](#author)

---

## Features

- Email/password register and login via Supabase Auth (BFF); Facebook OAuth callback
- JWT (HS256 / Supabase secret) on every protected route; roles `USER`, `ADMIN`, `TECHNICIAN`
- Public service catalog; admin CRUD for categories, services (with image data URLs), and promotion codes
- Apply promotion codes (`not found` / expired / quota full) then charge the payable amount with Omise
- Persist paid bookings as customer orders + technician `service_jobs`
- Customer order list, history, and reviews
- Technician queue: accept/decline, pending jobs, complete, account and location
- In-app notifications (`JOB_CREATED`, `JOB_ACCEPTED`, `JOB_COMPLETED`, …)
- Health endpoints for deploy probes

---

## Tech Stack

- **Backend:** Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Security (stateless), Bean Validation
- **Data:** Spring Data JPA, Hibernate, PostgreSQL (Supabase). Local optional: Docker Compose Postgres 16
- **Authentication:** Supabase Auth + JWT filter (`JwtAuthFilter`). Admin/technician checks in services
- **Payments:** Omise secret key on the server only
- **Libraries & tools:** Lombok, JJWT 0.12.6, HikariCP (small pool for Supabase), Maven Wrapper

---

## Project Structure

```text
home-service-app-server/
├── Dockerfile
├── docker-compose.yml          # local Postgres on host port 5433
├── pom.xml                     # java.version 21
├── .env.example
├── scripts/                    # seed SQL (admin, technician, promos, jobs)
└── src/main/java/.../home_service_app_server/
    ├── controller/             # REST endpoints
    ├── service/                # business rules + role checks
    ├── repository/
    ├── entity/
    ├── dto/
    ├── security/               # JwtAuthFilter, JwtService
    ├── client/                 # Supabase Auth HTTP client
    └── config/                 # SecurityConfig, CORS, dotenv
```

Default port: `8080` (`PORT` env). Tomcat POST body limit is 16MB so admin service images (data URLs) fit.

---

## Getting Started

**Needs:** JDK 21, Maven Wrapper (`./mvnw` or `mvnw.cmd`). Postgres: Supabase, or Compose below.

```bash
git clone <this-repo-url>
cd home-service-app-server
cp .env.example .env
# fill SPRING_DATASOURCE_*, SUPABASE_*, OMISE_*, CORS_ALLOWED_ORIGINS
./mvnw spring-boot:run
```

Windows: `.\mvnw.cmd spring-boot:run`

API: [http://localhost:8080](http://localhost:8080)  
Probe: `GET /health` → `{ "status": "ok" }`

Local Postgres (does not replace Supabase Auth):

```bash
docker compose up -d
```

Then point `SPRING_DATASOURCE_URL` at `localhost:5433` / database `home_service_app`.

Do not commit `.env`. Production env on Render overrides the file.

---

## Environment Variables

Names only. Copy `.env.example` — never paste real keys into git or this README.

| Variable | Purpose |
| --- | --- |
| `PORT` | HTTP port (default `8080`) |
| `SPRING_DATASOURCE_URL` | JDBC URL. Prefer Supabase **pooler**. Transaction pooler (6543) needs `prepareThreshold=0` (already in `application.properties`) |
| `SPRING_DATASOURCE_USERNAME` | Often `postgres.<project-ref>` on the pooler |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE` | Keep small on shared Supabase (default `2`) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Use `none` in shared/prod-like envs; local may use `update` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated Vue origins (localhost + Vercel) |
| `SUPABASE_URL` | `https://<ref>.supabase.co` |
| `SUPABASE_ANON_KEY` | Anon/public API key |
| `SUPABASE_JWT_SECRET` | JWT secret from Project Settings → API |
| `SUPABASE_SERVICE_ROLE_KEY` | Server-only; needed to create Auth users on register |
| `OMISE_PUBLIC_KEY` | `pkey_…` (optional on server; client uses its own) |
| `OMISE_SECRET_KEY` | `skey_…` — **never** ship to the browser |

---

## API Reference

Unless noted **public**, send `Authorization: Bearer <access_token>`.  
Admin and technician rules are enforced in services (`requireAdmin`, `requireCurrentTechnician`), not only in the security filter.

### Public

| Method | Path |
| --- | --- |
| `GET` | `/` |
| `GET` | `/health` |
| `POST` | `/api/auth/login` |
| `POST` | `/api/auth/register` |
| `GET` | `/api/auth/facebook?redirectTo=` |
| `POST` | `/api/auth/facebook` |
| `POST` | `/api/auth/logout` |
| `GET` | `/api/services` |
| `GET` | `/api/services/{id}` |

### Account (authenticated)

| Method | Path |
| --- | --- |
| `GET` / `PUT` / `PATCH` | `/api/users/me` |
| `PATCH` | `/api/users/me/password` |

### Booking and orders

| Method | Path | Notes |
| --- | --- | --- |
| `POST` | `/api/promotions/apply` | Body `{ "code", "amount" }`. Does not consume quota |
| `POST` | `/api/charges` | Omise token + booking; amount in baht (minimum 20) |
| `GET` | `/api/orders?scope=active\|history` | Current user |
| `POST` | `/api/orders/{jobId}/review` | Completed jobs only |

### Admin

| Method | Path |
| --- | --- |
| `GET` / `POST` | `/api/admin/categories` |
| `PATCH` | `/api/admin/categories` (reorder `{ "ids": [...] }`) |
| `GET` / `PATCH` / `DELETE` | `/api/admin/categories/{id}` |
| `GET` / `POST` | `/api/admin/services` |
| `PATCH` / `PUT` | `/api/admin/services` or `/reorder` |
| `GET` / `PATCH` / `DELETE` | `/api/admin/services/{id}` |
| `GET` / `POST` | `/api/admin/promotions` |
| `GET` / `PATCH` / `DELETE` | `/api/admin/promotions/{id}` |
| `GET` | `/api/notifications` |
| `GET` | `/api/notifications/unread-count` |
| `PATCH` | `/api/notifications/{id}/read` |

### Technician

| Method | Path |
| --- | --- |
| `GET` / `PATCH` | `/api/technician/account` |
| `POST` | `/api/technician/account/location` |
| `GET` | `/api/technician/requests` |
| `GET` | `/api/technician/requests/pending-count` |
| `POST` | `/api/technician/requests/{id}/accept` |
| `POST` | `/api/technician/requests/{id}/decline` |
| `GET` | `/api/technician/jobs/pending` |
| `GET` | `/api/technician/jobs/history` |
| `GET` | `/api/technician/jobs/{id}` |
| `POST` | `/api/technician/jobs/{id}/complete` |

Docker **must** compile with Java 21 (`pom.xml` `<java.version>21</java.version>`). A `25` release flag fails the image build (`release version 25 not supported`).

---

## Database

Schema is JPA entities against Supabase Postgres. There is no Flyway folder; optional SQL lives in `scripts/`:

| File | Use |
| --- | --- |
| `scripts/seed-admin.sql` | Admin user row (after Auth user exists) |
| `scripts/seed-technician.sql` | Technician seed |
| `scripts/promotions.sql` | Promotions table helpers |
| `scripts/service_options.sql` | Options |
| `scripts/seed-technician-jobs.sql` | Sample jobs |
| `scripts/drop-orders-status.sql` | One-off cleanup |

Core tables: `users`, `categories`, `services`, `service_options`, `promotions`, `orders` / `order_items`, `service_jobs`, `technician_profiles`, `technician_job_declines`, `notifications`.

---

## Deployment

`Dockerfile`: Maven 21 build (`./mvnw clean package -DskipTests`) then `eclipse-temurin:21-jre`. Exposes `8080`.

Typical Render (or similar) setup:

1. Connect this GitHub repo; build from `Dockerfile`.
2. Set the env vars in [Environment Variables](#environment-variables) on the host (not in git).
3. `CORS_ALLOWED_ORIGINS` must include the Vercel frontend origin.
4. Use the Supabase **pooler** URL and a small Hikari pool so you do not hit session limits.

---

## Related Repos

- Backend: this repository (`home-service-app-server`)
- Frontend: `home-service-app-client` (Vue 3 + Vite). Set `VITE_API_BASE_URL` to this API.

---

## Author

HomeServices team

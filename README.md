# PayVault — Review 1 Package

Covers rubric items: **Problem Analysis** (see `REQUIREMENT_ANALYSIS.md`),
**Microservice Identification & Service Discovery**, **JWT Authentication**,
**API Gateway Configuration**.

## What's inside

```
payvault-review1/
├── REQUIREMENT_ANALYSIS.md   <- rubric item 1
├── db-setup.sql               <- run in pgAdmin first
├── discovery-server/          <- Eureka, port 8761
├── auth-service/               <- register/login, JWT issuer, port 8081
├── user-service/               <- profile CRUD, port 8082
└── api-gateway/                <- single entry point, port 8080
```

## 1. Database setup (pgAdmin)

1. Open pgAdmin, connect to your local PostgreSQL server.
2. Right-click the **server** (not a specific database) → **Query Tool**.
3. Paste the contents of `db-setup.sql` and run it. This creates `auth_db`
   and `user_db`.
4. You don't need to create any tables by hand — each service has
   `spring.jpa.hibernate.ddl-auto=update`, so Hibernate creates them the
   first time each service starts.
5. If your local Postgres username/password isn't `postgres`/`postgres`,
   update `spring.datasource.username` / `password` in:
   - `auth-service/src/main/resources/application.yml`
   - `user-service/src/main/resources/application.yml`

## 2. Import into STS / IntelliJ

Import each of the four folders (`discovery-server`, `auth-service`,
`user-service`, `api-gateway`) as a separate **Existing Maven Project**.
Let each one download its dependencies on first import.

## 3. Run order (always this order)

1. **discovery-server** — wait until you see `Started DiscoveryServerApplication`
   Confirm at http://localhost:8761 — you should see the Eureka dashboard.
2. **auth-service** — wait for `Started AuthServiceApplication`
3. **user-service** — wait for `Started UserServiceApplication`
4. **api-gateway** — wait for `Started ApiGatewayApplication`

After all four are up, refresh http://localhost:8761 — you should see
`AUTH-SERVICE`, `USER-SERVICE`, and `API-GATEWAY` (gateway registers too)
listed as `UP`.

## 4. Test it (everything goes through the Gateway on port 8080)

**Register a user** (public, no token needed):
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"shanmukha","email":"shanmukha@test.com","password":"secret123"}'
```

**Login** (public, returns a JWT):
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"shanmukha","password":"secret123"}'
```
Copy the `token` value from the response.

**Call a protected endpoint** (needs the JWT):
```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <paste-token-here>"
```

**Create a profile** (also protected, goes to user-service):
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <paste-token-here>" \
  -H "Content-Type: application/json" \
  -d '{"authUsername":"shanmukha","fullName":"Shanmukha Krishna","email":"shanmukha@test.com","phone":"9999999999","address":"Rajahmundry"}'
```

**Try without a token** — should return `401 Unauthorized`:
```bash
curl http://localhost:8080/api/users
```

This 401 result is worth demonstrating live in your review — it's direct
proof the Gateway's JWT filter is actually doing its job, not just decoration.

## 5. What to say in the review

- **Microservice identification**: explain the Eureka / Gateway / Auth / User
  split and why Auth and User are separate (see section 5 of the requirement doc).
- **Service discovery**: show the Eureka dashboard with all services `UP`.
- **JWT auth**: show login returning a token, then a protected call failing
  without it and succeeding with it.
- **API Gateway**: show that nothing is called directly on 8081/8082 — every
  request goes through 8080, and mention that `lb://` routing gives you load
  balancing for free if you ever run two instances of a service.

## 6. Still to build (after Review 1)

- **Wallet Service** (`wallet_db`) — balance holding, credit/debit
- **Transaction Service** (`transaction_db`) — orchestrates transfers,
  calls Wallet Service twice (debit sender, credit receiver), writes ledger
- Unit & integration tests across all services
- Deployment step

Say the word and we'll build Wallet Service next the same way — zip,
run, verify.

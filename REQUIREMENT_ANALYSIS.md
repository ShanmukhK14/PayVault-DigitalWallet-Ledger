# PS020: Fault-Tolerant Digital Wallet & Real-Time Financial Ledger System
### Problem Analysis & Requirement Specification — Review 1

**Course:** 24SDCS03R — SOA Programming and Microservices
**Company (fictional client):** PayVault Financial

---

## 1. Business Use Case

PayVault Financial needs a digital wallet ecosystem that lets registered users hold a
balance, transfer money to one another in real time, and view their transaction
history. The system must guarantee **transaction security**, **strict balance
consistency** (no lost updates / no double-spending), **instant debit/credit
processing**, and **fault-tolerant ledger updates** even under high transaction
volume.

## 2. Actors

| Actor | Description |
|---|---|
| **Registered User** | Signs up, logs in, holds a wallet balance, sends/receives money |
| **System Admin** (future scope) | Monitors transactions, manages disputes |
| **PayVault Platform** | The microservice ecosystem itself, acting as the trusted intermediary for every transfer |

## 3. Functional Requirements

| ID | Requirement |
|---|---|
| FR1 | A user can register with a username, email, and password |
| FR2 | A user can log in and receive a signed JWT access token |
| FR3 | A user can create/view/update their profile (name, email, phone, address) |
| FR4 | A user can view their wallet balance *(Review 2/3 scope)* |
| FR5 | A user can transfer funds to another user's wallet *(Review 2/3 scope)* |
| FR6 | Every transfer is recorded immutably in a transaction ledger *(Review 2/3 scope)* |
| FR7 | All financial and profile endpoints require a valid JWT |

## 4. Non-Functional Requirements

| ID | Requirement | How it's addressed |
|---|---|---|
| NFR1 | **Security** | Passwords hashed with BCrypt; all traffic authenticated via JWT (HS256), validated at both the API Gateway edge and inside each downstream service |
| NFR2 | **Service Discovery / Fault Tolerance** | All services self-register with a Eureka server; the Gateway resolves service instances dynamically, so a service can restart/scale without hardcoded addresses breaking |
| NFR3 | **Single Entry Point** | Spring Cloud Gateway is the only externally exposed port; internal services are not called directly by clients |
| NFR4 | **Load Balancing** | Gateway routes use `lb://SERVICE-NAME`, letting Spring Cloud LoadBalancer round-robin across multiple instances of the same service registered in Eureka |
| NFR5 | **Data Isolation** | Each microservice owns a private PostgreSQL database — no service reaches into another service's tables directly |
| NFR6 | **Consistency** | Balance-affecting operations will be confined to the Wallet Service so all debit/credit logic lives in one transactional boundary (Review 2/3) |

## 5. Microservice Identification

The system is decomposed by business capability, following single-responsibility
boundaries:

| Service | Responsibility | Owns DB |
|---|---|---|
| **Eureka Server** | Service registry — every other service registers here | — |
| **API Gateway** | Single entry point; routes requests; validates JWT at the edge; load balances across instances | — |
| **Auth Service** | Registration, login, JWT issuance | `auth_db` |
| **User Service** | User profile CRUD | `user_db` |
| **Wallet Service** *(Review 2/3)* | Holds balances, applies credits/debits | `wallet_db` |
| **Transaction Service** *(Review 2/3)* | Orchestrates transfers, writes ledger history | `transaction_db` |

**Why split Auth from User?** Authentication credentials (password hashes) are a
different security domain from profile data (name, address, KYC status). Keeping
them in separate services/databases limits the blast radius if one is compromised,
and mirrors how real fintech systems separate an identity provider from a profile
store.

## 6. Request Flow (Review 1 scope)

```
Client
  │  POST /api/auth/register  (public)
  │  POST /api/auth/login     (public)  -> returns JWT
  ▼
API Gateway  (validates JWT on every other route)
  │
  ├─ /api/auth/**  → lb://AUTH-SERVICE  → auth_db
  └─ /api/users/** → lb://USER-SERVICE  → user_db
```

## 7. Request Flow (planned for full project — Review 2/3)

```
Client → API Gateway → Transaction Service
                             │
                    ┌────────┴────────┐
                    ▼                 ▼
              Wallet Service    Wallet Service
              (debit sender)   (credit receiver)
                    │
              wallet_db (ledger entry written by Transaction Service)
```

## 8. Technology Stack

- Java 17, Spring Boot 3.2.5, Spring Cloud 2023.0.1
- Spring Cloud Netflix Eureka (service discovery)
- Spring Cloud Gateway (reactive, WebFlux-based routing + load balancing)
- Spring Security + JJWT 0.11.5 (HS256 signed tokens)
- Spring Data JPA + PostgreSQL (one schema per service)
- Maven

## 9. Fault Tolerance Considerations

- Services register/deregister with Eureka independently — the Gateway never needs
  a restart to pick up a new or replaced instance.
- Eureka's self-preservation is disabled in dev (`enable-self-preservation: false`)
  so stale instances are evicted quickly during testing; this would be reconsidered
  for a production network-partition scenario.
- Each service fails independently: if User Service goes down, Auth Service (login)
  keeps working, and the Gateway simply returns a 503 for `/api/users/**` rather
  than the whole system going down.

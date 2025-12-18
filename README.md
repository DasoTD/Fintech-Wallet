# Fintech Wallet 💳

**Simple fintech wallet service** built with Spring Boot (Java). This repository contains a lightweight wallet backend supporting user registration, authentication (JWT), basic wallet operations, Stripe integration, rate-limiting, IP/Geo restrictions (optional), and H2 in-memory DB for development.

---

## 🚀 Quick start

Prerequisites
- Java 21 (recommended) or newer (project is configured for Java 21 in `pom.xml`) 
- Maven

Build & run
```bash
# build
mvn -DskipTests package

# run
mvn -DskipTests spring-boot:run
```

The app starts on http://localhost:8080 by default.

---

## ⚙️ Configuration

Key configuration is in `src/main/resources/application.yml`.
Important environment variables / properties you may want to set:
- `STRIPE_SECRET_KEY` – required for Stripe integration
- `GEOIP_DB_PATH` – (optional) path to GeoLite2-Country.mmdb if you want geo restrictions enabled. Alternatively place `GeoLite2-Country.mmdb` in `src/main/resources/`.

Notes
- The app uses an H2 in-memory DB by default for development. No external DB required to get started.
- Lombok is used (annotation processing); enable Lombok in your IDE.

---

## 🔐 Authentication

- Register: POST `/auth/register` — body: JSON user (username, password, etc.)
- Login: POST `/auth/login` — body: `{ "username": "...", "password": "..." }`
  - Response is a JWT token string.

Example (curl)
```bash
# register
curl -X POST http://localhost:8080/auth/register -H 'Content-Type: application/json' -d '{"username":"alice","password":"secret"}'

# login
curl -X POST http://localhost:8080/auth/login -H 'Content-Type: application/json' -d '{"username":"alice","password":"secret"}'
```

Use the returned token in `Authorization: Bearer <token>` for authenticated requests.

---

## 🧪 Tests

Run unit tests
```bash
mvn test
```

---

## 📝 Developer notes

- GeoIP: If you don't include the GeoIP database, geo-restrictions are disabled by default.
- Rate limiting uses Bucket4j — configuration is in `RateLimitConfig` and properties.
- Security: `SecurityConfig` configures JWT and a DaoAuthenticationProvider using `UserService`.
- If you see failures about `AuthenticationManager` or missing beans, ensure configuration classes are picked up by component scanning.

---

## 📂 Useful files
- `src/main/java/com/example/fintechwallet/controller` — REST controllers
- `src/main/java/com/example/fintechwallet/service` — business logic
- `src/main/java/com/example/fintechwallet/repository` — Spring Data JPA repositories
- `src/main/resources/application.yml` — application settings

---

## 🤝 Contributing
Contributions welcome — open an issue or a PR. Keep changes small and add tests for new behavior.

---

## 📬 Contact & License
- Author: DasoTD (see repository)
- License: MIT (or change to your preferred license)

---

If you'd like, I can add a short contributing guide, GitHub Actions for CI, or integration tests for the auth flow. Which one should I do next? 🔧
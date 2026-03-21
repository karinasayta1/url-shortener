# URL Shortener with Custom Domains

A production‑ready URL shortener built with **Spring Boot**, **MySQL**, **Redis**, **Docker**, and **Jenkins**. It supports **custom domains** with DNS verification, asynchronous click counting, Redis caching, and a fully automated CI/CD pipeline.

## Features

- ✅ Shorten any long URL to a unique 6‑character code (Base62 encoding).
- ✅ Redirect to original URL with HTTP 302.
- ✅ **Custom domains** – use your own domain (e.g., `go.mycompany.com`) and verify ownership via DNS TXT record.
- ✅ Asynchronous click counting – no delay during redirects.
- ✅ Redis caching for lightning‑fast redirects.
- ✅ REST API with JSON payload.
- ✅ Dockerized with MySQL, Redis, and the app in separate containers.
- ✅ Jenkins CI/CD pipeline (automated build, test, Docker image creation, and deployment).
- ✅ Environment‑based configuration (easy to switch between local and production).

## Tech Stack

| Component        | Technology                                      |
|------------------|-------------------------------------------------|
| Backend          | Java 17, Spring Boot 3.1.5                      |
| Database         | MySQL 8.0 (JPA/Hibernate)                       |
| Cache            | Redis 7 (Spring Cache)                          |
| Containerization | Docker, Docker Compose                          |
| CI/CD            | Jenkins (declarative pipeline)                  |
| Build Tool       | Maven                                           |
| Utility          | Lombok, Base62 encoding, SecureRandom           |

## Prerequisites

- Java 17 JDK
- Maven 3.8+
- Docker Desktop (for containerized run)
- Git
- Postman or `curl` (for testing)
- A text editor (IntelliJ IDEA recommended)

## Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/karinasayta1/url-shortener.git
cd url-shortener
```

### 2. Build the application (without Docker)
```bash
mvn clean package
```
This creates the JAR file in `target/`.

### 3. Run with Docker Compose (recommended)
Start all services (MySQL, Redis, and the app) in containers:
```bash
docker-compose up --build
```
The app will be available at `http://localhost:8081`.

- MySQL: `localhost:3307` (user: `user`, password: `userpassword`)
- Redis: `localhost:6380`

To stop everything:
```bash
docker-compose down
```

### 4. Test the core API
Create a short URL:
```bash
curl -X POST http://localhost:8081/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://www.example.com"}'
```
Response:
```json
{
  "shortCode": "aB3dEf",
  "shortUrl": "http://localhost:8081/aB3dEf",
  "longUrl": "https://www.example.com",
  "createdAt": "2026-03-21T12:00:00"
}
```
Visit the returned `shortUrl` – you'll be redirected.

## Custom Domains Feature

This URL shortener allows you to use your own domain for branded links. The process is similar to services like Bitly or Rebrandly.

### How It Works
1. **Register your domain** – `POST /domains/register` with your domain name.
2. **Verify ownership** – The server returns a token. Add a DNS TXT record (`_urlshortener.yourdomain.com` → token) to prove you own the domain.
3. **Verify the domain** – `POST /domains/verify` with the token. The domain becomes `verified` in the database.
4. **Create short URLs** – Include the `customDomain` field in the `/shorten` request.
5. **Redirect** – The app listens to the `Host` header and serves links from your custom domain.

### Testing Locally with a  Domain
1. Edit your hosts file (`/etc/hosts` or `C:\Windows\System32\drivers\etc\hosts`) and add:
   ```
   127.0.0.1 mydomain.local
   ```
2. Register the domain:
   ```bash
   curl -X POST http://localhost:8081/domains/register \
     -H "Content-Type: application/json" \
     -d '{"domainName": "mydomain.local"}'
   ```
   Note the `token` in the response.
3. Verify the domain (replace `<token>` with your token):
   ```bash
   curl -X POST http://localhost:8081/domains/verify \
     -H "Content-Type: application/json" \
     -d '{"domainName": "mydomain.local", "token": "<token>"}'
   ```
4. Create a short URL with that domain:
   ```bash
   curl -X POST http://localhost:8081/shorten \
     -H "Content-Type: application/json" \
     -d '{"longUrl": "https://www.google.com", "customDomain": "mydomain.local"}'
   ```
   The response will contain `"shortUrl": "http://mydomain.local:8081/abc123"`.
5. Visit that URL in your browser – it will redirect to Google.

## API Endpoints

| Method | Endpoint               | Description                                      | Request Body                                 |
|--------|------------------------|--------------------------------------------------|----------------------------------------------|
| POST   | `/shorten`             | Create a short URL.                              | `{"longUrl": "...", "expirationDays": 7, "customDomain": "..."}` |
| GET    | `/{shortCode}`         | Redirect to original URL.                        | –                                            |
| POST   | `/domains/register`    | Register a new custom domain.                    | `{"domainName": "..."}`                      |
| POST   | `/domains/verify`      | Verify a domain after adding DNS TXT record.     | `{"domainName": "...", "token": "..."}`      |
| GET    | `/domains/status`      | Check if a domain is verified.                   | `?domainName=...` (query param)              |

## Project Structure

```
urlshortener/
├── src/main/java/com/example/urlshortner/
│   ├── controller/           # REST endpoints
│   │   ├── UrlController.java
│   │   └── DomainController.java
│   ├── dto/                  # Data transfer objects
│   │   ├── UrlRequest.java
│   │   └── UrlResponse.java
│   ├── entity/               # JPA entities
│   │   ├── Url.java
│   │   └── Domain.java
│   ├── repository/           # Spring Data JPA repositories
│   │   ├── UrlRepository.java
│   │   └── DomainRepository.java
│   ├── service/              # Business logic
│   │   ├── UrlService.java
│   │   ├── ClickService.java
│   │   └── DomainService.java
│   ├── util/                 # Utilities
│   │   └── Base62.java
│   └── UrlshortenerApplication.java
├── src/main/resources/
│   └── application.properties
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
├── pom.xml
└── README.md
```

## Environment Variables

The following environment variables can be set to customize the application:

| Variable                     | Description                             | Default                 |
|------------------------------|-----------------------------------------|-------------------------|
| `SPRING_DATASOURCE_URL`      | MySQL JDBC URL                          | `jdbc:mysql://...`      |
| `SPRING_DATASOURCE_USERNAME` | MySQL username                          | `root`                  |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password                          | –                       |
| `SPRING_DATA_REDIS_HOST`     | Redis host                              | `localhost`             |
| `SPRING_DATA_REDIS_PORT`     | Redis port                              | `6379`                  |
| `APP_PORT`                   | Port used when building short URLs      | `8081`                  |

These are already set in `docker-compose.yml` to match the container names.

## Jenkins CI/CD Pipeline

The repository includes a `Jenkinsfile` that defines a declarative pipeline. When changes are pushed to the `main` branch, Jenkins will:

1. Checkout the code.
2. Build the JAR with Maven.
3. Build a Docker image (`urlshortener:latest`).
4. (Optional) Stop and remove the old container.
5. Run a new container using the image, connecting to the existing MySQL and Redis containers.

To use the pipeline:
- Set up a Jenkins instance (e.g., using the provided Docker command).
- Install the Maven, Docker, and Git plugins.
- Create a new pipeline job pointing to this repository.
- Run the pipeline manually or let it trigger on push.

## Running Locally (without Docker)

If you prefer to run the app directly on your machine (for development), you need:

- MySQL server running on port 3306 with a database named `urlshortener`.
- Redis server running on port 6379.
- Update `src/main/resources/application.properties` with your credentials.

Then build and run:
```bash
mvn spring-boot:run
```

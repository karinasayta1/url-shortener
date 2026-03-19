# URL Shortener Service

A scalable URL shortening service built with Spring Boot, MySQL, Redis, and Docker. It provides REST APIs to create short URLs, redirect to original URLs, and track click analytics. The project demonstrates backend development, caching, asynchronous processing, containerization, and CI/CD with Jenkins.

## Features

- Generate a unique short code for any long URL (Base62 encoding).
- Redirect from short URL to original URL with HTTP 302.
- Optional URL expiration (TTL).
- Asynchronous click counting for performance.
- Redis caching for fast lookups.
- RESTful API design.
- Dockerized for easy deployment.
- Jenkins pipeline for CI/CD (optional).

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.1.5, Spring Data JPA, Spring Cache, Lombok
- **Database**: MySQL 8.0
- **Cache**: Redis 7
- **Build Tool**: Maven
- **Containerization**: Docker, Docker Compose
- **CI/CD**: Jenkins (Jenkinsfile included)

## Prerequisites

- Java 17 JDK
- Maven 3.8+
- Docker and Docker Compose (for containerized run)
- Git
- Postman or curl (for testing)
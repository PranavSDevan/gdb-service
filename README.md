# GDB Banking Service

[![CI Pipeline](https://github.com/PranavSDevan/gdb-service/actions/workflows/ci.yml/badge.svg)](https://github.com/PranavSDevan/gdb-service/actions/workflows/ci.yml)
[![Release](https://github.com/PranavSDevan/gdb-service/actions/workflows/release.yml/badge.svg)](https://github.com/PranavSDevan/gdb-service/actions/workflows/release.yml)

**GDB (Global Digital Bank)** is a full-stack microservices-based digital banking platform built with Java 17 and Spring Boot 3.4. It simulates a real-world core banking system with account management, payment processing, credit cards, AI-powered chat, and more.

---

## Architecture Overview

The platform follows a **microservices architecture** with a dedicated database per service (Database-per-Service pattern):

| Service | Port | Description |
|---|---|---|
| gateway-service | 8000 | API Gateway (Spring Cloud Gateway) |
| ccount-service | 8001 | Savings & current account management |
| 	ransactions-service | 8002 | Ledger, fund transfers, transaction logs |
| users-service | 8003 | User registration and KYC |
| uth-service | 8004 | JWT authentication and session management |
| adhar-service | 8005 | Aadhar identity verification |
| company-service | 8006 | Company CIN registration validation |
| payment-gateway-service | 8008 | Payment processing (UPI, NEFT, RTGS, IMPS) |
| credit-cards-service | 8010 | Credit card issuance and transactions |
| ank-statements-service | 8011 | PDF/CSV statement generation |
| settings-service | 8012 | User preferences and theme settings |
| i-service | 8016 | GDB Copilot (Gemini AI-powered chat assistant) |
| eureka-server | 8761 | Service discovery registry |
| rontend | 3000 | React web portal |

---

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.4, Spring Cloud, Spring Data JPA
- **Databases:** PostgreSQL 15 (one database per service)
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Frontend:** React 18, Vite, TailwindCSS
- **AI:** Google Gemini 2.5 Flash API
- **Testing:** JUnit 5, Mockito, Spring Boot Test (MockMvc)
- **Build:** Apache Maven (multi-module)
- **Containerization:** Docker, Docker Compose
- **CI/CD:** GitHub Actions

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 15+
- Node.js 18+
- Docker Desktop

### Quick Start (Docker)

`ash
# Clone the repository
git clone https://github.com/PranavSDevan/gdb-service.git
cd gdb-service

# Set your Gemini API key
echo "GEMINI_API_KEY=your_key_here" > .env

# Start all services
docker-compose up --build -d
`

Access the web portal at **http://localhost:3000**

### Running Tests

`ash
# Run all unit tests across all services
mvn test

# Run tests for a specific service
mvn test -pl auth-service
`

---

## CI/CD

| Workflow | Trigger | Purpose |
|---|---|---|
| **CI Pipeline** | Push / PR to master, dev, eature/** | Builds and runs all unit tests |
| **Release** | Push tag *.*.* | Builds JARs, packages as .tar.gz, publishes GitHub Release |

### Creating a Release

`ash
git tag v1.0.0
git push origin v1.0.0
`

This automatically triggers the Release workflow and publishes a GitHub Release with all service JARs bundled as a .tar.gz archive.

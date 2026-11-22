# Employee Management Platform & Automated Validation Suite

[![Dev CI & E2E Quality Gate](https://github.com/dhanushnagendraprasad/employee-management/actions/workflows/deploy-dev.yml/badge.svg)](https://github.com/dhanushnagendraprasad/employee-management/actions/workflows/deploy-dev.yml)
[![Production Deployment](https://github.com/dhanushnagendraprasad/employee-management/actions/workflows/deploy.yml/badge.svg)](https://github.com/dhanushnagendraprasad/employee-management/actions/workflows/deploy.yml)
![Java 21](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)
![React 19](https://img.shields.io/badge/React-19.2-blue.svg)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6.svg)
![Playwright](https://img.shields.io/badge/Playwright-1.49-green.svg)
![Compliance](https://img.shields.io/badge/Compliance-FDA%2021%20CFR%20Part%2011%20%2F%20GAMP%205-purple.svg)

An enterprise-grade full-stack human resource management platform featuring an isolated multi-environment deployment architecture, zero-skip hermetic backend test suites, and an automated Playwright E2E validation engine generating FDA 21 CFR Part 11 / GAMP 5 compliant audit reports with cryptographic SHA-256 integrity verification.

---

## 🌐 Live Environments & Infrastructure

The application runs on a dual-environment Linux server architecture with complete database and network isolation:

| Environment | Purpose | Ingress / URL | Backend Port | Database | Service Name |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Production** | Live system for end users | [Live App Ingress](https://quickness-bony-striking.ngrok-free.dev) | `8081` | `employee_management` (MySQL) | `employee-management.service` |
| **Development** | Integration testing & E2E gate | `http://localhost:8080` (or LAN IP) | `8082` | `employee_management_dev` (MySQL) | `employee-management-dev.service` |

---

## 🏗️ System Architecture & Technology Stack

```
                              SYSTEM ARCHITECTURE
                              
   [ Vite Frontend: React 19 + TypeScript ] ◄──► [ Playwright E2E Test Suite ]
                         │                                    │
                    REST │ Axios (JWT Auth)                   │ UI & API Probing
                         ▼                                    ▼
       [ Spring Boot 3.3.5 REST API Engine ] ────► [ GxP 21 CFR Part 11 Reporter ]
         ├── Spring Security + JJWT 0.11.5                    ├── Executive Summary Cards
         ├── Spring Data JPA & Hibernate                      ├── Step Screenshot Audits
         └── Role-Based Access (ADMIN, MANAGER, EMPLOYEE)     └── SHA-256 Digital Seal
                         │
        ┌────────────────┴────────────────┐
        ▼                                 ▼
   [ Dev MySQL (Port 8082) ]    [ Prod MySQL (Port 8081) ]
```

### Backend Engine
- **Runtime:** Java 21 LTS
- **Framework:** Spring Boot 3.3.5 (Spring Web, Spring Security, Spring Data JPA)
- **Authentication:** Stateless JWT (`io.jsonwebtoken:jjwt`), BCrypt password hashing
- **Database:** MySQL 8.0 (Runtime) & H2 In-Memory (Hermetic Unit Testing)
- **Build Tool:** Maven with integrated testing lifecycle

### Modern Frontend
- **Framework:** React 19 SPA (Single Page Application)
- **Tooling:** Vite 8, TypeScript 5, React Router v7
- **State Management:** Zustand
- **Styling & UI:** Pure CSS Modules with glassmorphism design, Lucide React icons

### Quality Engineering & Test Architecture
- **Backend Testing:** 64 hermetic JUnit 5 & Spring MockMvc integration tests running in-memory with `@ActiveProfiles("test")` (0 skipped, 100% pass rate).
- **E2E Automation:** Playwright + TypeScript framework implementing strict Page Object Model (`LoginPage`, `RegisterPage`, `DashboardPage`).
- **Dynamic Event-Driven Synchronization:** Zero arbitrary sleeps (`waitForTimeout`). All waits rely on DOM element lifecycle, absence of loading indicators, and browser animation settlement (`document.getAnimations().map(a => a.finished)`).
- **Regulatory Validation Reporter:** Custom Playwright reporter generating executive validation summaries (HTML & companion PDF) featuring chronological audit logs, full-width high-resolution evidence screenshots, and an immutable 256-bit SHA-256 cryptographic integrity hash.

---

## 🛡️ Enterprise CI/CD & Dev Quality Gate

The repository uses GitHub Actions branch protection where **code cannot reach Production without passing through the Dev E2E Quality Gate**:

```
                       CI/CD QUALITY GATE LIFECYCLE
                       
  Developer commits to `dev` branch
               │
               ▼
  [ GitHub Actions: deploy-dev.yml ]
    ├── 1. Run 64 Backend Unit/Integration Tests (H2 in-memory, 0 skips)
    ├── 2. Build Vite Frontend & Spring Boot JAR package
    ├── 3. Restart Dev systemd service (`employee-management-dev`)
    │
    └── 4. 🛡️ PLAYWRIGHT E2E REGRESSION SUITE (Dev Gate)
             ├── Executes multi-step validation protocols against running Dev
             ├── Computes 256-bit SHA-256 Checksum on generated audit report
             ├── Attaches HTML & PDF reports as 14-day GitHub Artifacts
             │
             ├── IF ALL PASS:
             │     └── PR to `main` approved for merge
             │
             └── IF ANY FAIL:
                   └── Build fails ❌; Merge to `main` is HARD BLOCKED
```

---

## 📋 4-Step Regulatory Validation Protocol (CSV Standard)

In compliance with life sciences Computer System Validation (CSV) protocols, every test protocol establishes an immutable visual audit trail across four execution phases:

1. **Step 1: Initial State** — Proves pristine, empty initial form navigation state.
2. **Step 2: Input Population** — Proves accurate user input entry prior to submission.
3. **Step 3: Verification Outcome** — Captures completed state (directory loaded with no spinners, or server rejection banner fully rendered).
4. **Step 4: Session Teardown** — Clicks `Logout`, validates session termination, and captures proof of return to the login portal.

---

## 🔌 Public REST API Reference

All protected endpoints require an `Authorization: Bearer <token>` header obtained via `/api/auth/login`.

| # | Endpoint | Method | Role Required | Description |
| :- | :--- | :---: | :---: | :--- |
| **1** | `/api/auth/register` | `POST` | Public | Register a new user account (employee, manager, or admin) |
| **2** | `/api/auth/login` | `POST` | Public | Authenticate credentials and receive JWT access token |
| **3** | `/api/employees` | `POST` | `ADMIN` | Create a new employee record with address and salary |
| **4** | `/api/employees` | `GET` | `ADMIN`, `MANAGER` | Retrieve paginated list of employees (`page`, `size`, `sortBy`) |
| **5** | `/api/employees/{id}` | `GET` | Authenticated | Retrieve specific employee profile by ID |
| **6** | `/api/employees/{id}` | `PUT` | `ADMIN`, `MANAGER` | Update existing employee details |
| **7** | `/api/employees/{id}/salary` | `PUT` | `ADMIN`, `MANAGER` | Apply percentage-based salary hike (`?hikePercentage=X`) |
| **8** | `/api/employees/{id}` | `DELETE` | `ADMIN` | Permanently delete an employee record |
| **9** | `/api/employees/search/city/{city}` | `GET` | `ADMIN`, `MANAGER` | Search employees by city |
| **10** | `/api/employees/search/state/{state}` | `GET` | `ADMIN`, `MANAGER` | Search employees by state |
| **11** | `/api/employees/search/country/{country}` | `GET` | `ADMIN`, `MANAGER` | Search employees by country |
| **12** | `/api/employees/search/pincode/{pincode}` | `GET` | `ADMIN`, `MANAGER` | Search employees by postal code |
| **13** | `/api/employees/search/role/{role}` | `GET` | `ADMIN`, `MANAGER` | Filter employees by assigned role (`ROLE_EMPLOYEE`, etc.) |
| **14** | `/api/employees/search/salary/above/{amt}` | `GET` | `ADMIN`, `MANAGER` | Filter employees with salary strictly above amount |
| **15** | `/api/employees/search/salary/below/{amt}` | `GET` | `ADMIN`, `MANAGER` | Filter employees with salary strictly below amount |

---

## 🚀 Local Development Quickstart

### Prerequisites
- Java 21 JDK
- Node.js (v20+) & npm
- MySQL 8.0 (or run with H2 profile)

### 1. Run Backend Service
```powershell
# Run with local MySQL profile
./mvnw spring-boot:run

# Or run hermetic test suite (H2 in-memory)
./mvnw test
```

### 2. Run Modern Frontend (Vite)
```powershell
cd frontend
npm install
npm run dev
# Frontend runs at http://localhost:5173
```

### 3. Run Playwright E2E Validation Suite
```powershell
cd e2e
npm install
npx playwright install chromium

# Run full regression suite (headless)
npx playwright test

# Or watch browser in real time
npx playwright test --headed
```
Generated validation reports (HTML & PDF) and screenshot audit trails are saved to `e2e/reports/`.

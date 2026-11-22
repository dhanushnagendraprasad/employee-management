# Comprehensive Test Architecture & Validation Documentation

An authoritative technical reference for the test strategy, execution architecture, and regulatory validation suites implemented in the Employee Management Platform.

---

## 🏛️ Test Strategy & The Test Pyramid

The application enforces a multi-tier test pyramid ensuring high test execution speed, deterministic verification, and full regulatory traceability:

```
                          THE TEST PYRAMID
                          
                     ▲
                    / \      [ 4 E2E Validation Protocols ]
                   /   \     Playwright + TypeScript (Dev UI/API Gate)
                  /     \    BMS GxP Standard / 21 CFR Part 11 HTML+PDF Reports
                 /───────\   
                /         \  [ 15 Integration Tests ]
               /           \ Spring MockMvc + Full Security & JWT Workflow
              /─────────────\
             /               \ [ 49 Unit & Service Tests ]
            /                 \ JUnit 5 + Mockito + In-Memory H2 DB
           /───────────────────\
```

| Layer | Framework / Tools | Scope | Database | Execution Time | Gate Policy |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Backend Unit & MockMvc** | JUnit 5, Mockito, MockMvc | Controllers, Business Services, Security Filters | H2 In-Memory (`@ActiveProfiles("test")`) | ~3.5s (64 tests) | Zero skip, 100% pass required on every commit |
| **Backend Integration** | SpringBootTest, MockMvc | Full registration, login, and CRUD lifecycles | H2 In-Memory | Included in Maven build | Gated on `mvn test` |
| **Frontend E2E Validation** | Playwright, TypeScript | Full UI session lifecycle, authentication, CSV audits | Live Dev MySQL (Port 8082) | ~12.4s (4 protocols) | Hard block on PR merge to `main` |

---

## ☕ 1. Backend Test Suite (64 Hermetic Tests)

All backend tests execute hermetically using Spring Boot's test profile with **zero external dependencies** and **zero data leakage**.

### Configuration Isolation (`src/test/resources/application-test.properties`)
* **Database:** In-memory H2 (`jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1`)
* **DDL Strategy:** `create-drop` (fresh schema per test suite, cleanly destroyed after execution)
* **Profile Annotation:** Enforced via `@ActiveProfiles("test")` on all test classes to prevent fallback to production MySQL.

---

### Layer Breakdown

#### A. Controller Layer Tests (`src/test/java/.../controller/`)
1. **`AuthControllerTest.java` (6 Test Cases)**
   - `testRegisterUser_Success()` — Valid registration returns 200 OK.
   - `testRegisterUser_WithoutRole_DefaultsToEmployee()` — Verifies default fallback role assignment.
   - `testRegisterUser_MissingRequiredFields()` — Input validation error handling.
   - `testLoginUser_Success()` — Valid credentials return JWT access token and username.
   - `testLoginUser_InvalidCredentials()` — Bad credentials return 401 Unauthorized / Bad credentials exception.
   - `testLoginUser_UserNotFound()` — Non-existent user handling.

2. **`EmployeeControllerTest.java` (30 Test Cases)**
   - **Create:** Admin authorized (201 Created), Manager forbidden (403), Unauthenticated rejected (401).
   - **Read & Paging:** Pagination with `page`, `size`, `sortBy` parameters, ID lookup, not-found handling (404).
   - **Update:** Admin update, Manager update, Employee role forbidden (403).
   - **Salary Hike:** Percentage calculation, negative percentage handling, RBAC permissions.
   - **Delete:** Admin deletion (204 No Content), non-admin rejection (403).
   - **Location & Attribute Search:** Filter by city, state, country, pincode, role, salary above threshold, salary below threshold.

#### B. Service Business Logic Layer Tests (`src/test/java/.../service/`)
1. **`AuthServiceTest.java` (5 Test Cases)**
   - Password encryption verification (`BCryptPasswordEncoder`).
   - Role assignment logic (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_EMPLOYEE`).
   - Duplicate username prevention (`existsByUsername` throwing `RuntimeException`).

2. **`EmployeeServiceTest.java` (13 Test Cases)**
   - Employee creation with composite embedded `Address`.
   - Update verification (including email, address, and salary fields).
   - Percentage-based salary hike calculations.
   - Custom search queries and pagination business logic.

#### C. End-to-End Integration Tests (`src/test/java/.../`)
1. **`EmployeeManagementIntegrationTest.java` (9 Workflows)**
   - Complete multi-step user registration, JWT token acquisition, and authenticated CRUD workflow.
   - Role-Based Access Control (RBAC) security matrix validation.
   - Public vs. Protected endpoint security barriers.

2. **`EmployeeManagementApplicationTests.java` (1 Test Case)**
   - Spring ApplicationContext initialization and dependency injection smoke test.

---

## 🎭 2. Playwright E2E Regression Validation Suite

The E2E automation suite is located under `e2e/` and executes against the deployed Development environment (`http://localhost:8080` or `http://192.168.1.106:8080`).

### Design Patterns
* **Strict Page Object Model (POM):**
  * `LoginPage.ts`: Encapsulates username, password, login action, and error banners.
  * `RegisterPage.ts`: Encapsulates registration fields, role selection, submit, and alert banners.
  * `DashboardPage.ts`: Encapsulates directory table, data fetch state, and clean session logout.
  * Zero direct DOM selectors exist in test spec files.
* **Test Isolation & Hermetic Data:**
  * Positive tests dynamically generate unique timestamped users (`user_${Date.now()}`) to prevent state collisions.
  * Positive login tests provision a fresh test user via fast API calls (`page.request.post`) in milliseconds before initiating UI login, eliminating dependency on dirty database state.
* **Dynamic Event-Driven Synchronization:**
  * Zero arbitrary `waitForTimeout` sleeps.
  * Dynamic waits observe incoming page header visibility, spinner disappearance, and browser animation settlement via `document.getAnimations().map(a => a.finished)`.

---

### Executed Validation Protocols

| Protocol ID | Scope | Verification Type | Lifecycle Phases Verified |
| :--- | :--- | :---: | :--- |
| **`TC-VAL-01`** | Positive Authentication & Session Lifecycle | Functional / GxP | 1. Empty Login Form $\rightarrow$ 2. Populated Credentials $\rightarrow$ 3. Authenticated Dashboard (Data Loaded) $\rightarrow$ 4. Logout Proof |
| **`TC-VAL-02`** | Negative Authentication (Bad Credentials) | Security / Error | 1. Empty Form $\rightarrow$ 2. Populated Invalid Data $\rightarrow$ 3. Server Rejection (`Bad credentials`) |
| **`TC-VAL-03`** | Positive Account Provisioning & Dynamic Redirect | Functional / Flow | 1. Empty Signup $\rightarrow$ 2. Populated Data $\rightarrow$ 3. Success Banner $\rightarrow$ 4. Settled Redirect to Login |
| **`TC-VAL-04`** | Negative Registration on Duplicate Username | Data Integrity | 1. Fresh Form $\rightarrow$ 2. Populated Duplicate Data $\rightarrow$ 3. Collision Banner (`Username exists`) |

---

## 🔒 3. Regulatory GxP / FDA 21 CFR Part 11 Reporting

The test suite incorporates a custom regulatory reporter (`e2e/reporters/gxp-reporter.ts`) generating tamper-evident execution audit trails.

### Compliance Standards Met
* **Attributable:** Operator identity explicitly recorded (`Dhanush Nagendra Prasad`).
* **Contemporaneous:** Execution timestamps formatted in Indian Standard Time (`GMT+5:30`) per validation step.
* **Original & Accurate:** 100% full-width high-resolution screenshot evidence captured at every phase of test execution.
* **Tamper-Evident:** A 256-bit cryptographic **SHA-256 Checksum** is computed across the generated report body and embedded in the digital seal. Any post-execution tampering invalidates the hash.
* **Automated Artifacts:** Generates both a standalone single-file HTML report (embedded base64 evidence) and a companion A4 PDF document compiled via headless Chromium.
* **Storage Governance:** Automated local retention policy keeps only the **latest 5 runs**, preventing server storage exhaustion.

---

## 🚀 4. How to Execute Tests

### Backend Suite
```powershell
# Run all 64 backend tests
./mvnw clean test

# Run a specific test class
./mvnw test -Dtest=EmployeeControllerTest

# Run a single test method
./mvnw test -Dtest=AuthControllerTest#testLoginUser_Success
```

### Playwright E2E Suite
```powershell
cd e2e

# Run all validation protocols (headless)
npx playwright test

# Run with interactive UI mode
npx playwright test --ui

# Run with headed browser visible
npx playwright test --headed
```
Reports and screenshot audit trails are generated under `e2e/reports/`.

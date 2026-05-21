# Employee Management API

This document lists the application's public REST APIs with real-world names, numbered entries, endpoints, request/response shapes and authorization notes. Use this as the quick reference for API documentation and README inclusion.

---

## How to run (dev)

- Run with Maven wrapper:

```powershell
./mvnw spring-boot:run
```

- The application uses MySQL by default (see `src/main/resources/application.properties`). To run quickly without MySQL, create a `application-dev.properties` profile that points to H2 and start with `-Dspring.profiles.active=dev`.

- Swagger/OpenAPI UI: http://localhost:8080/swagger-ui.html (when app running)

---

## Authentication

- This app uses JWT-based authentication. Obtain a token using the Login API and send it in the `Authorization` header for protected endpoints:

```
Authorization: Bearer <token>
```

---

## Numbered API reference

1) User Registration (Register User)
   - Method: POST
   - Path: `/api/auth/register`
   - Purpose: Create a new user account (employee or other role).
   - Request body: `SignupRequest` (fields used in controller: `username`, `password`, `email`, `salary`, `address`, optional `role`)
   - Response: result of `AuthService.register(...)` (200 OK)
   - Auth: Public (no token required)

2) User Login (Authenticate / Issue JWT)
   - Method: POST
   - Path: `/api/auth/login`
   - Purpose: Authenticate user and receive JWT token for subsequent requests.
   - Request body: `LoginRequest` (`username`, `password`)
   - Response: `JwtResponse` (`token`, `username`) (200 OK)
   - Auth: Public (no token required)

3) Create Employee (Add Employee)
   - Method: POST
   - Path: `/api/employees`
   - Purpose: Create a new employee record.
   - Request body: `EmployeeDTO` (see `src/main/java/.../dto/EmployeeDTO.java`)
   - Response: Created `AppUser` (201 Created)
   - Auth: ADMIN only (`hasRole('ADMIN')`)

4) List Employees (Paged)
   - Method: GET
   - Path: `/api/employees`
   - Purpose: Get paginated list of employees.
   - Query params: `page` (default 0), `size` (default 10), `sortBy` (default `id`), `sortDir` (`asc` or `desc`)
   - Response: `Page<AppUser>` (200 OK)
   - Auth: ADMIN or MANAGER (`hasAnyRole('ADMIN', 'MANAGER')`)

5) Get Employee By ID
   - Method: GET
   - Path: `/api/employees/{id}`
   - Purpose: Retrieve a specific employee by database id.
   - Response: `AppUser` (200 OK) or 404 Not Found
   - Auth: Any authenticated user (`isAuthenticated()`)

6) Update Employee
   - Method: PUT
   - Path: `/api/employees/{id}`
   - Purpose: Update an employee entity.
   - Request body: `AppUser` (entity payload)
   - Response: updated `AppUser` (200 OK) or 404 Not Found
   - Auth: ADMIN or MANAGER

7) Apply Salary Hike (Adjust Salary)
   - Method: PUT
   - Path: `/api/employees/{id}/salary?hikePercentage=<value>`
   - Purpose: Apply a percentage-based salary hike to employee.
   - Query param: `hikePercentage` (double)
   - Response: updated `AppUser` (200 OK) or 404 Not Found
   - Auth: ADMIN or MANAGER

8) Delete Employee
   - Method: DELETE
   - Path: `/api/employees/{id}`
   - Purpose: Remove an employee record.
   - Response: 204 No Content
   - Auth: ADMIN only

9) Search Employees by City
   - Method: GET
   - Path: `/api/employees/search/city/{city}`
   - Purpose: Find employees by city.
   - Response: `List<AppUser>`
   - Auth: ADMIN or MANAGER

10) Search Employees by State
    - Method: GET
    - Path: `/api/employees/search/state/{state}`
    - Purpose: Find employees by state.
    - Response: `List<AppUser>`
    - Auth: ADMIN or MANAGER

11) Search Employees by Country
    - Method: GET
    - Path: `/api/employees/search/country/{country}`
    - Purpose: Find employees by country.
    - Response: `List<AppUser>`
    - Auth: ADMIN or MANAGER

12) Search Employees by Pincode
    - Method: GET
    - Path: `/api/employees/search/pincode/{pincode}`
    - Purpose: Find employees by postal code.
    - Response: `List<AppUser>`
    - Auth: ADMIN or MANAGER

13) Search Employees by Role
    - Method: GET
    - Path: `/api/employees/search/role/{roleName}`
    - Purpose: Find employees that have a specific role (e.g., ADMIN, MANAGER).
    - Note: controller will convert `ADMIN` to `ROLE_ADMIN` if needed.
    - Response: `List<AppUser>`
    - Auth: ADMIN or MANAGER

14) Search Employees by Salary Above
    - Method: GET
    - Path: `/api/employees/search/salary/above/{amount}`
    - Purpose: Find employees with salary strictly above amount.
    - Response: `List<AppUser>`
    - Auth: ADMIN or MANAGER

15) Search Employees by Salary Below
    - Method: GET
    - Path: `/api/employees/search/salary/below/{amount}`
    - Purpose: Find employees with salary strictly below amount.
    - Response: `List<AppUser>`
    - Auth: ADMIN or MANAGER

16) Test: Verify Password (Debug)
    - Method: POST
    - Path: `/api/test/verify`
    - Purpose: Debug endpoint that checks whether a provided password matches stored hash for a user.
    - Request body: `LoginRequest` (`username`, `password`)
    - Response: Plain text debug report (not JSON). Do not expose in production.
    - Auth: none (controller not annotated for security), use with caution.

---

## Notes & next steps

- The above request/response DTO types are located in `src/main/java/com/example/employee_management/dto` and `payload` packages. Use the Java classes for exact field names and validation annotations.
- Consider adding example curl/PowerShell calls for each API and improving the README with sample request bodies. I can add those if you want.
- Consider removing or protecting the `/api/test/verify` endpoint before production.

---

If you want, I can now:
- add example requests and sample responses for each API in this README, or
- generate a small Postman collection from these endpoints, or
- add a developer `application-dev.properties` for H2 and document running with it.

---

## Examples (for Postman; assumes local server at http://localhost:8081)

Note: The project defaults to port 8080. If you started the app on 8081 (as in the earlier logs), replace 8081 with 8080 when appropriate.

Common role names (seeded by `DataSeeder`):
- ROLE_ADMIN
- ROLE_MANAGER
- ROLE_EMPLOYEE

The examples below show the HTTP method and the full endpoint URL to add into Postman. JSON payloads are provided where relevant.

1) Login (get JWT)
    - Method: POST
    - URL: http://localhost:8081/api/auth/login
    - Body (JSON):
```json
{
   "username": "admin",
   "password": "password"
}
```

2) Register (create user)
    - Method: POST
    - URL: http://localhost:8081/api/auth/register
    - Body (JSON):
```json
{
   "username": "newuser",
   "password": "changeme",
   "email": "newuser@example.com",
   "salary": 45000,
   "address": {
      "city": "Bengaluru",
      "state": "Karnataka",
      "country": "India",
      "pincode": "560001"
   },
   "role": "ROLE_EMPLOYEE"
}
```

3) Create Employee (admin only)
    - Method: POST
    - URL: http://localhost:8081/api/employees
    - Headers: Authorization: Bearer <token>
    - Body (JSON, matches `EmployeeDTO`):
```json
{
   "username": "employee1",
   "password": "secret123",
   "email": "employee1@example.com",
   "role": "ROLE_EMPLOYEE",
   "salary": 35000,
   "levelNo": 2,
   "address": {
      "city": "Chennai",
      "state": "Tamil Nadu",
      "country": "India",
      "pincode": "600001"
   }
}
```

4) List employees (paged)
    - Method: GET
    - URL: http://localhost:8081/api/employees?page=0&size=10&sortBy=id&sortDir=asc
    - Headers: Authorization: Bearer <token>

5) Get employee by id
    - Method: GET
    - URL: http://localhost:8081/api/employees/1
    - Headers: Authorization: Bearer <token>

6) Update employee (admin/manager)
    - Method: PUT
    - URL: http://localhost:8081/api/employees/1
    - Headers: Authorization: Bearer <token>
    - Body: provide the full `AppUser` JSON (take from GET, modify fields)

7) Apply salary hike
    - Method: PUT
    - URL: http://localhost:8081/api/employees/1/salary?hikePercentage=10
    - Headers: Authorization: Bearer <token>

8) Delete employee (admin)
    - Method: DELETE
    - URL: http://localhost:8081/api/employees/1
    - Headers: Authorization: Bearer <token>

9) Search examples
    - By city:
       - Method: GET
       - URL: http://localhost:8081/api/employees/search/city/Bengaluru
       - Headers: Authorization: Bearer <token>
    - By role (example: managers):
       - Method: GET
       - URL: http://localhost:8081/api/employees/search/role/MANAGER
       - Headers: Authorization: Bearer <token>

10) Debug: verify password (use cautiously)
      - Method: POST
      - URL: http://localhost:8081/api/test/verify
      - Body (JSON):
```json
{
   "username": "admin",
   "password": "password"
}
```

---

If you want, I can add example JSON files (`register.json`, `employee.json`, `updated-employee.json`) to the repo so you can upload them to Postman or import them directly. Tell me which examples you'd like added and I'll commit them.

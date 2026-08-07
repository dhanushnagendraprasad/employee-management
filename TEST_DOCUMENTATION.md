# Employee Management API - Test Documentation

## Overview
This document provides comprehensive information about the test suite for the Employee Management API. The test suite covers unit tests, integration tests, and service layer tests.

## Test Structure

```
src/test/java/com/example/employee_management/
├── controller/
│   ├── AuthControllerTest.java          # Authentication controller tests
│   └── EmployeeControllerTest.java      # Employee CRUD controller tests
├── service/
│   ├── AuthServiceTest.java             # Authentication service tests
│   └── EmployeeServiceTest.java         # Employee service business logic tests
├── EmployeeManagementIntegrationTest.java  # End-to-end integration tests
└── EmployeeManagementApplicationTests.java # Basic application context test

src/test/resources/
└── application-test.properties          # Test configuration using H2 in-memory DB
```

## Test Categories

### 1. Controller Layer Tests

#### AuthControllerTest.java
Tests authentication endpoints with focus on HTTP status codes and response content.

**Test Cases:**
- `testRegisterUser_Success()` - Valid user registration
- `testRegisterUser_WithoutRole_DefaultsToEmployee()` - Default role assignment
- `testRegisterUser_MissingRequiredFields()` - Input validation
- `testLoginUser_Success()` - Valid login with JWT token generation
- `testLoginUser_InvalidCredentials()` - Authentication failure
- `testLoginUser_UserNotFound()` - Non-existent user handling

**Endpoints Tested:**
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User authentication

#### EmployeeControllerTest.java
Tests employee CRUD operations with role-based access control.

**Test Cases:**

*Create Employee:*
- `testCreateEmployee_WithAdminRole_Success()` - Admin can create
- `testCreateEmployee_WithManagerRole_Forbidden()` - Manager cannot create
- `testCreateEmployee_Unauthorized()` - Unauthenticated access denied

*Read Employees:*
- `testGetAllEmployees_WithAdminRole_Success()` - Admin list access
- `testGetAllEmployees_WithManagerRole_Success()` - Manager list access
- `testGetAllEmployees_WithEmployeeRole_Forbidden()` - Employee denied
- `testGetAllEmployees_WithPagination()` - Pagination support
- `testGetAllEmployees_WithSortBySalary()` - Sort by different fields
- `testGetEmployeeById_Authenticated_Success()` - Authenticated access
- `testGetEmployeeById_NotFound()` - Non-existent employee
- `testGetEmployeeById_Unauthorized()` - Unauthenticated access

*Update Employee:*
- `testUpdateEmployee_WithAdminRole_Success()` - Admin update
- `testUpdateEmployee_WithManagerRole_Success()` - Manager update
- `testUpdateEmployee_WithEmployeeRole_Forbidden()` - Employee denied
- `testUpdateEmployee_NotFound()` - Update non-existent employee

*Salary Hike:*
- `testApplySalaryHike_WithAdminRole_Success()` - Admin hike
- `testApplySalaryHike_WithManagerRole_Success()` - Manager hike
- `testApplySalaryHike_WithEmployeeRole_Forbidden()` - Employee denied
- `testApplySalaryHike_WithNegativePercentage()` - Negative hike handling

*Delete Employee:*
- `testDeleteEmployee_WithAdminRole_Success()` - Admin delete
- `testDeleteEmployee_WithManagerRole_Forbidden()` - Manager denied
- `testDeleteEmployee_WithEmployeeRole_Forbidden()` - Employee denied

*Search Operations:*
- `testSearchByCity_Success()` - Search by city
- `testSearchByCity_WithManagerRole_Success()` - Manager search access
- `testSearchByCity_Unauthorized()` - Unauthenticated search
- `testSearchByState_Success()` - Search by state
- `testSearchByCountry_Success()` - Search by country
- `testSearchByPincode_Success()` - Search by pincode
- `testSearchByRole_Success()` - Search by role
- `testSearchBySalaryAbove_Success()` - Search by salary range (above)
- `testSearchBySalaryBelow_Success()` - Search by salary range (below)

**Endpoints Tested:**
- `POST /api/employees` - Create employee
- `GET /api/employees` - List employees (with pagination)
- `GET /api/employees/{id}` - Get employee by ID
- `PUT /api/employees/{id}` - Update employee
- `PUT /api/employees/{id}/salary` - Apply salary hike
- `DELETE /api/employees/{id}` - Delete employee
- `GET /api/employees/search/city/{city}` - Search by city
- `GET /api/employees/search/state/{state}` - Search by state
- `GET /api/employees/search/country/{country}` - Search by country
- `GET /api/employees/search/pincode/{pincode}` - Search by pincode
- `GET /api/employees/search/role/{roleName}` - Search by role
- `GET /api/employees/search/salary/above/{amount}` - Search by salary above
- `GET /api/employees/search/salary/below/{amount}` - Search by salary below

### 2. Service Layer Tests

#### AuthServiceTest.java
Tests business logic for authentication and user registration.

**Test Cases:**
- `testRegisterUser_Success()` - Successful registration
- `testRegisterUser_PasswordEncrypted()` - Password encryption verification
- `testRegisterUser_WithAdminRole()` - Admin role assignment
- `testRegisterUser_WithManagerRole()` - Manager role assignment
- `testRegisterUser_DuplicateUsername()` - Duplicate username prevention

**Methods Tested:**
- `AuthService.register(AppUser, String)` - User registration with role assignment

#### EmployeeServiceTest.java
Tests business logic for employee CRUD operations.

**Test Cases:**
- `testCreateEmployee_Success()` - Create employee with validation
- `testGetEmployeeById_Success()` - Retrieve employee
- `testGetEmployeeById_NotFound()` - Handle non-existent employee
- `testUpdateEmployee_Success()` - Update employee data
- `testApplySalaryHike_Success()` - Calculate and apply salary hike
- `testApplySalaryHike_WithNegativePercentage()` - Handle negative adjustments
- `testDeleteEmployee_Success()` - Delete employee and verify removal
- `testSearchByCity_Success()` - Search functionality by city
- `testSearchByState_Success()` - Search functionality by state
- `testSearchByCountry_Success()` - Search functionality by country
- `testSearchByPincode_Success()` - Search functionality by pincode
- `testSearchBySalaryAbove_Success()` - Search by salary threshold (above)
- `testSearchBySalaryBelow_Success()` - Search by salary threshold (below)

**Methods Tested:**
- `EmployeeService.createEmployee(EmployeeDTO)`
- `EmployeeService.getEmployeeById(Long)`
- `EmployeeService.updateEmployee(Long, AppUser)`
- `EmployeeService.applySalaryHike(Long, double)`
- `EmployeeService.deleteEmployee(Long)`
- `EmployeeService.searchByCity(String)`
- `EmployeeService.searchByState(String)`
- `EmployeeService.searchByCountry(String)`
- `EmployeeService.searchByPincode(String)`
- `EmployeeService.searchBySalaryAbove(Double)`
- `EmployeeService.searchBySalaryBelow(Double)`

### 3. Integration Tests

#### EmployeeManagementIntegrationTest.java
End-to-end tests covering complete workflows with real authentication.

**Test Cases:**
- `testFullRegistrationFlow()` - Registration with all details
- `testCompleteEmployeeCRUDWorkflow()` - Full CRUD lifecycle
  - Create employee
  - Retrieve employee
  - Apply salary hike
  - Search by location
  - Delete employee
  - Verify deletion
- `testRoleBasedAccessControl()` - RBAC verification for different roles
- `testSearchFunctionality()` - All search endpoints
- `testPaginationSupport()` - Pagination and sorting
- `testUnauthorizedAccess()` - Security: unauthenticated requests
- `testAuthenticationWithInvalidCredentials()` - Security: invalid credentials

**Workflows Tested:**
1. **Authentication Flow**
   - User registration
   - User login with JWT token generation
   - Token validation

2. **Employee Management Flow**
   - Create new employee (Admin only)
   - List employees with pagination
   - Retrieve specific employee
   - Update employee information
   - Apply salary adjustments
   - Search employees by various criteria
   - Delete employees

3. **Role-Based Access Control**
   - Admin: Full access to all operations
   - Manager: Access to read and update operations
   - Employee: Limited to viewing own data

4. **Security**
   - JWT token authentication
   - Role-based authorization
   - Unauthorized access rejection
   - Invalid credential handling

## Running the Tests

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=EmployeeControllerTest
mvn test -Dtest=EmployeeServiceTest
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=EmployeeManagementIntegrationTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=AuthControllerTest#testLoginUser_Success
mvn test -Dtest=EmployeeControllerTest#testCreateEmployee_WithAdminRole_Success
```

### Run Tests with Coverage
```bash
mvn clean test jacoco:report
# Report available at: target/site/jacoco/index.html
```

### Run Tests from IDE
- Right-click on test class → Run 'ClassName'
- Right-click on test method → Run 'methodName()'
- Or use Ctrl+Shift+F10 (Windows/Linux) or Ctrl+Shift+R (Mac)

## Test Configuration

**Test Database:** H2 In-Memory Database
- **URL:** `jdbc:h2:mem:testdb`
- **Driver:** `org.h2.Driver`
- **Schema Creation:** `create-drop` (created before tests, dropped after)

**Test Properties File:** `application-test.properties`
- Uses H2 instead of MySQL
- Logging level set to WARN for cleaner output
- JWT configuration matches production

**Active Profiles:** `@ActiveProfiles("test")`
- Loads `application-test.properties` during test execution
- Isolated from production database
- Fast execution with in-memory DB

## Test Data Setup

### Pre-seeded Users (via DataSeeder)
- **admin** / password (ROLE_ADMIN)
- **manager** / password (ROLE_MANAGER)
- **employee** / password (ROLE_EMPLOYEE)

### Dynamic Test Data
- Created in `@BeforeEach` methods
- Cleaned up automatically after each test
- Ensures test isolation and repeatability

## Test Assertions

Common assertion patterns used:
```java
// Status assertions
.andExpect(status().isOk())
.andExpect(status().isCreated())
.andExpect(status().isNotFound())
.andExpect(status().isUnauthorized())
.andExpect(status().isForbidden())
.andExpect(status().isNoContent())

// JSON path assertions
.andExpect(jsonPath("$.id").exists())
.andExpect(jsonPath("$.username").value("testuser"))
.andExpect(jsonPath("$.salary").value(50000.0))

// Service layer assertions
assertEquals(expected, actual)
assertNotNull(result)
assertTrue(condition)
assertThrows(ExceptionClass.class, () -> methodCall())
```

## Coverage Goals

- **Controller Tests:** 90%+ coverage
- **Service Tests:** 95%+ coverage
- **Integration Tests:** Key workflows covered
- **Overall:** 85%+ code coverage

## Best Practices Used

1. **Isolation:** Each test is independent and can run in any order
2. **Clarity:** Test names clearly describe what is being tested
3. **Completeness:** Both success and failure scenarios covered
4. **Performance:** Tests run quickly using in-memory DB
5. **Maintainability:** Organized by layers (controller, service, integration)
6. **Security:** Authentication and authorization tested
7. **Documentation:** Comments explain test purpose

## Continuous Integration

These tests are designed to run in CI/CD pipelines:
- Fast execution (~30 seconds for full suite)
- No external dependencies (in-memory DB)
- Deterministic results
- Easy to integrate with GitHub Actions, Jenkins, etc.

## Troubleshooting

### Test Failures

**Issue:** `DataIntegrityViolationException` on duplicate username
**Solution:** Ensure `@BeforeEach` clears test data

**Issue:** `NullPointerException` in assertions
**Solution:** Use null-safe assertions or check for null before assertions

**Issue:** JWT token expiration in integration tests
**Solution:** Tokens are generated fresh in `@BeforeEach` for each test

**Issue:** Tests pass locally but fail in CI
**Solution:** Check timezone settings, ensure H2 configuration is correct

## Future Enhancements

- [ ] Add performance/load tests
- [ ] Add security penetration tests
- [ ] Add database migration tests
- [ ] Add API contract testing
- [ ] Add mutation testing for code quality
- [ ] Add API documentation tests

## References

- JUnit 5 Documentation: https://junit.org/junit5/docs/current/user-guide/
- Spring Boot Testing: https://spring.io/guides/gs/testing-web/
- MockMvc Documentation: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/web/servlet/MockMvc.html
- Hamcrest Matchers: http://hamcrest.org/


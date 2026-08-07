package com.example.employee_management;

import com.example.employee_management.dto.AddressDTO;
import com.example.employee_management.dto.EmployeeDTO;
import com.example.employee_management.model.Address;
import com.example.employee_management.payload.JwtResponse;
import com.example.employee_management.payload.LoginRequest;
import com.example.employee_management.payload.SignupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the entire Employee Management API.
 * Tests the complete flow: Registration -> Login -> CRUD Operations
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.employee_management.repository.AppUserRepository appUserRepository;

    private String adminToken;
    private String employeeToken;
    private String managerToken;

    @BeforeEach
    void setUp() throws Exception {
        appUserRepository.deleteAll();
        
        // Register Admin
        SignupRequest adminSignup = new SignupRequest();
        adminSignup.setUsername("admin");
        adminSignup.setPassword("password");
        adminSignup.setEmail("admin@example.com");
        adminSignup.setRole("ROLE_ADMIN");
        adminSignup.setSalary(100000.0);
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminSignup)));

        // Login as admin to get token
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername("admin");
        adminLogin.setPassword("password");

        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse adminResponse = objectMapper.readValue(
                adminResult.getResponse().getContentAsString(),
                JwtResponse.class);
        adminToken = adminResponse.getToken();

        // Register Manager
        SignupRequest managerSignup = new SignupRequest();
        managerSignup.setUsername("manager");
        managerSignup.setPassword("password");
        managerSignup.setEmail("manager@example.com");
        managerSignup.setRole("ROLE_MANAGER");
        managerSignup.setSalary(80000.0);
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(managerSignup)));

        // Login as manager to get token
        LoginRequest managerLogin = new LoginRequest();
        managerLogin.setUsername("manager");
        managerLogin.setPassword("password");

        MvcResult managerResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(managerLogin)))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse managerResponse = objectMapper.readValue(
                managerResult.getResponse().getContentAsString(),
                JwtResponse.class);
        managerToken = managerResponse.getToken();
    }

    /**
     * Test: User Registration Flow
     */
    @Test
    void testFullRegistrationFlow() throws Exception {
        Address address = new Address();
        address.setCity("Mumbai");
        address.setState("Maharashtra");
        address.setCountry("India");
        address.setPincode("400001");

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newemployee");
        signupRequest.setPassword("newpass123");
        signupRequest.setEmail("newemployee@example.com");
        signupRequest.setSalary(45000.0);
        signupRequest.setAddress(address);
        signupRequest.setRole("ROLE_EMPLOYEE");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    /**
     * Test: Complete CRUD Workflow with Authentication
     */
    @Test
    void testCompleteEmployeeCRUDWorkflow() throws Exception {
        // 1. Create new employee
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCity("Delhi");
        addressDTO.setState("Delhi");
        addressDTO.setCountry("India");
        addressDTO.setPincode("110001");

        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setUsername("crudemployee");
        employeeDTO.setPassword("pass123");
        employeeDTO.setEmail("crud@example.com");
        employeeDTO.setRole("ROLE_EMPLOYEE");
        employeeDTO.setSalary(50000.0);
        employeeDTO.setLevelNo(2);
        employeeDTO.setAddress(addressDTO);

        MvcResult createResult = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract employee ID from response
        String responseContent = createResult.getResponse().getContentAsString();
        String employeeId = objectMapper.readTree(responseContent).get("id").asText();

        // 2. Retrieve employee by ID
        mockMvc.perform(get("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("crud@example.com"));

        // 3. Apply salary hike
        mockMvc.perform(put("/api/employees/" + employeeId + "/salary")
                .header("Authorization", "Bearer " + adminToken)
                .param("hikePercentage", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary").value(55000.0));

        // 4. Search by city
        mockMvc.perform(get("/api/employees/search/city/Delhi")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 5. Delete employee
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 6. Verify employee is deleted
        mockMvc.perform(get("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Role-Based Access Control
     */
    @Test
    void testRoleBasedAccessControl() throws Exception {
        // Admin can list employees
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Manager can list employees
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Search Functionality
     */
    @Test
    void testSearchFunctionality() throws Exception {
        // Search by city
        mockMvc.perform(get("/api/employees/search/city/Bengaluru")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Search by state
        mockMvc.perform(get("/api/employees/search/state/Karnataka")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Search by country
        mockMvc.perform(get("/api/employees/search/country/India")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Search by pincode
        mockMvc.perform(get("/api/employees/search/pincode/560001")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Search by role
        mockMvc.perform(get("/api/employees/search/role/ADMIN")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Search by salary above
        mockMvc.perform(get("/api/employees/search/salary/above/30000")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Search by salary below
        mockMvc.perform(get("/api/employees/search/salary/below/100000")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Pagination Support
     */
    @Test
    void testPaginationSupport() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "salary")
                .param("sortDir", "desc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Security - Unauthorized Access
     */
    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Test: Authentication with Invalid Credentials
     */
    @Test
    void testAuthenticationWithInvalidCredentials() throws Exception {
        LoginRequest invalidLogin = new LoginRequest();
        invalidLogin.setUsername("admin");
        invalidLogin.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized());
    }
}

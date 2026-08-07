package com.example.employee_management.controller;

import com.example.employee_management.model.Address;
import com.example.employee_management.model.AppUser;
import com.example.employee_management.payload.JwtResponse;
import com.example.employee_management.payload.LoginRequest;
import com.example.employee_management.payload.SignupRequest;
import com.example.employee_management.service.AuthService;
import com.example.employee_management.security.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.employee_management.repository.AppUserRepository appUserRepository;

    @Autowired
    private com.example.employee_management.service.AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
        
        // Setup test data
        Address address = new Address();
        address.setCity("Bengaluru");
        address.setState("Karnataka");
        address.setCountry("India");
        address.setPincode("560001");

        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setPassword("testpass123");
        signupRequest.setEmail("testuser@example.com");
        signupRequest.setSalary(50000.0);
        signupRequest.setAddress(address);
        signupRequest.setRole("ROLE_EMPLOYEE");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("password");

        // Register the admin user for login tests
        AppUser adminUser = new AppUser();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setEmail("admin@example.com");
        adminUser.setSalary(100000.0);
        authService.register(adminUser, "ROLE_ADMIN");
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterUser_WithoutRole_DefaultsToEmployee() throws Exception {
        signupRequest.setRole(null);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterUser_MissingRequiredFields() throws Exception {
        signupRequest.setUsername(null);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginUser_Success() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void testLoginUser_InvalidCredentials() throws Exception {
        loginRequest.setPassword("wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginUser_UserNotFound() throws Exception {
        loginRequest.setUsername("nonexistent");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}

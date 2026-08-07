package com.example.employee_management.controller;

import com.example.employee_management.dto.AddressDTO;
import com.example.employee_management.dto.EmployeeDTO;
import com.example.employee_management.model.AppUser;
import com.example.employee_management.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.example.employee_management.repository.AppUserRepository appUserRepository;

    @Autowired
    private com.example.employee_management.service.AuthService authService;

    private EmployeeDTO employeeDTO;
    private AppUser testEmployee;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCity("Bengaluru");
        addressDTO.setState("Karnataka");
        addressDTO.setCountry("India");
        addressDTO.setPincode("560001");

        employeeDTO = new EmployeeDTO();
        employeeDTO.setUsername("emp1");
        employeeDTO.setPassword("pass123");
        employeeDTO.setEmail("emp1@example.com");
        employeeDTO.setRole("ROLE_EMPLOYEE");
        employeeDTO.setSalary(40000.0);
        employeeDTO.setLevelNo(2);
        employeeDTO.setAddress(addressDTO);

        AppUser newEmployee = new AppUser();
        newEmployee.setUsername("emp1");
        newEmployee.setPassword("pass123");
        newEmployee.setEmail("emp1@example.com");
        newEmployee.setSalary(40000.0);
        testEmployee = authService.register(newEmployee, "ROLE_EMPLOYEE");
    }

    // --- CREATE EMPLOYEE TESTS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_WithAdminRole_Success() throws Exception {
        employeeDTO.setUsername("new_emp");
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testCreateEmployee_WithManagerRole_Forbidden() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateEmployee_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)))
                .andExpect(status().isForbidden());
    }

    // --- GET ALL EMPLOYEES TESTS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_WithAdminRole_Success() throws Exception {
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetAllEmployees_WithManagerRole_Success() throws Exception {
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetAllEmployees_WithEmployeeRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllEmployees_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_WithPagination() throws Exception {
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("sortDir", "asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllEmployees_WithSortBySalary() throws Exception {
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "salary")
                .param("sortDir", "desc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- GET EMPLOYEE BY ID TESTS ---

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetEmployeeById_Authenticated_Success() throws Exception {
        mockMvc.perform(get("/api/employees/" + testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployeeById_NotFound() throws Exception {
        mockMvc.perform(get("/api/employees/9999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetEmployeeById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees/" + testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // --- UPDATE EMPLOYEE TESTS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_WithAdminRole_Success() throws Exception {
        testEmployee.setSalary(45000.0);
        mockMvc.perform(put("/api/employees/" + testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateEmployee_WithManagerRole_Success() throws Exception {
        testEmployee.setSalary(45000.0);
        mockMvc.perform(put("/api/employees/" + testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testUpdateEmployee_WithEmployeeRole_Forbidden() throws Exception {
        testEmployee.setSalary(45000.0);
        mockMvc.perform(put("/api/employees/" + testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_NotFound() throws Exception {
        testEmployee.setId(9999L);
        mockMvc.perform(put("/api/employees/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployee)))
                .andExpect(status().isNotFound());
    }

    // --- SALARY HIKE TESTS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void testApplySalaryHike_WithAdminRole_Success() throws Exception {
        mockMvc.perform(put("/api/employees/" + testEmployee.getId() + "/salary")
                .param("hikePercentage", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testApplySalaryHike_WithManagerRole_Success() throws Exception {
        mockMvc.perform(put("/api/employees/" + testEmployee.getId() + "/salary")
                .param("hikePercentage", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testApplySalaryHike_WithEmployeeRole_Forbidden() throws Exception {
        mockMvc.perform(put("/api/employees/" + testEmployee.getId() + "/salary")
                .param("hikePercentage", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testApplySalaryHike_WithNegativePercentage() throws Exception {
        mockMvc.perform(put("/api/employees/" + testEmployee.getId() + "/salary")
                .param("hikePercentage", "-10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // Should be handled by service
    }

    // --- DELETE EMPLOYEE TESTS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_WithAdminRole_Success() throws Exception {
        mockMvc.perform(delete("/api/employees/" + testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDeleteEmployee_WithManagerRole_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/" + testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testDeleteEmployee_WithEmployeeRole_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/" + testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // --- SEARCH BY CITY TESTS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchByCity_Success() throws Exception {
        mockMvc.perform(get("/api/employees/search/city/Bengaluru")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testSearchByCity_WithManagerRole_Success() throws Exception {
        mockMvc.perform(get("/api/employees/search/city/Chennai")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchByCity_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/employees/search/city/Bengaluru")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // --- SEARCH BY STATE TESTS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchByState_Success() throws Exception {
        mockMvc.perform(get("/api/employees/search/state/Karnataka")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- SEARCH BY COUNTRY TESTS ---

    @Test
    @WithMockUser(roles = "MANAGER")
    void testSearchByCountry_Success() throws Exception {
        mockMvc.perform(get("/api/employees/search/country/India")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- SEARCH BY PINCODE TESTS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchByPincode_Success() throws Exception {
        mockMvc.perform(get("/api/employees/search/pincode/560001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- SEARCH BY ROLE TESTS ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchByRole_Success() throws Exception {
        mockMvc.perform(get("/api/employees/search/role/ADMIN")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- SEARCH BY SALARY TESTS ---

    @Test
    @WithMockUser(roles = "MANAGER")
    void testSearchBySalaryAbove_Success() throws Exception {
        mockMvc.perform(get("/api/employees/search/salary/above/30000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchBySalaryBelow_Success() throws Exception {
        mockMvc.perform(get("/api/employees/search/salary/below/50000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

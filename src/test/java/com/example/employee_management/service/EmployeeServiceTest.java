package com.example.employee_management.service;

import com.example.employee_management.dto.AddressDTO;
import com.example.employee_management.dto.EmployeeDTO;
import com.example.employee_management.model.Address;
import com.example.employee_management.model.AppUser;
import com.example.employee_management.model.Role;
import com.example.employee_management.repository.AppUserRepository;
import com.example.employee_management.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    private EmployeeDTO testEmployeeDTO;
    private AppUser testEmployee;

    @BeforeEach
    void setUp() {
        // Clear test data
        appUserRepository.deleteAll();

        // Create test address
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCity("Bengaluru");
        addressDTO.setState("Karnataka");
        addressDTO.setCountry("India");
        addressDTO.setPincode("560001");

        // Create test employee DTO
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setUsername("testemployee");
        testEmployeeDTO.setPassword("testpass123");
        testEmployeeDTO.setEmail("testemployee@example.com");
        testEmployeeDTO.setRole("ROLE_EMPLOYEE");
        testEmployeeDTO.setSalary(50000.0);
        testEmployeeDTO.setLevelNo(2);
        testEmployeeDTO.setAddress(addressDTO);
    }

    @Test
    void testCreateEmployee_Success() {
        AppUser createdEmployee = employeeService.createEmployee(testEmployeeDTO);

        assertNotNull(createdEmployee);
        assertNotNull(createdEmployee.getId());
        assertEquals("testemployee", createdEmployee.getUsername());
        assertEquals("testemployee@example.com", createdEmployee.getEmail());
        assertEquals(50000.0, createdEmployee.getSalary());
    }

    @Test
    void testGetEmployeeById_Success() {
        AppUser createdEmployee = employeeService.createEmployee(testEmployeeDTO);
        AppUser retrievedEmployee = employeeService.getEmployeeById(createdEmployee.getId());

        assertNotNull(retrievedEmployee);
        assertEquals(createdEmployee.getId(), retrievedEmployee.getId());
        assertEquals("testemployee", retrievedEmployee.getUsername());
    }

    @Test
    void testGetEmployeeById_NotFound() {
        AppUser retrievedEmployee = employeeService.getEmployeeById(9999L);
        assertNull(retrievedEmployee);
    }

    @Test
    void testUpdateEmployee_Success() {
        AppUser createdEmployee = employeeService.createEmployee(testEmployeeDTO);
        
        AppUser updatedData = new AppUser();
        updatedData.setEmail("newemail@example.com");
        updatedData.setSalary(55000.0);

        AppUser updatedEmployee = employeeService.updateEmployee(createdEmployee.getId(), updatedData);

        assertNotNull(updatedEmployee);
        assertEquals("newemail@example.com", updatedEmployee.getEmail());
        assertEquals(55000.0, updatedEmployee.getSalary());
    }

    @Test
    void testApplySalaryHike_Success() {
        AppUser createdEmployee = employeeService.createEmployee(testEmployeeDTO);
        Double originalSalary = createdEmployee.getSalary();

        AppUser hikedEmployee = employeeService.applySalaryHike(createdEmployee.getId(), 10);

        assertNotNull(hikedEmployee);
        Double expectedSalary = originalSalary * 1.10;
        assertEquals(expectedSalary, hikedEmployee.getSalary(), 0.01);
    }

    @Test
    void testApplySalaryHike_WithNegativePercentage() {
        AppUser createdEmployee = employeeService.createEmployee(testEmployeeDTO);
        Double originalSalary = createdEmployee.getSalary();

        AppUser hikedEmployee = employeeService.applySalaryHike(createdEmployee.getId(), -5);

        assertNotNull(hikedEmployee);
        Double expectedSalary = originalSalary * 0.95;
        assertEquals(expectedSalary, hikedEmployee.getSalary(), 0.01);
    }

    @Test
    void testDeleteEmployee_Success() {
        AppUser createdEmployee = employeeService.createEmployee(testEmployeeDTO);
        Long employeeId = createdEmployee.getId();

        employeeService.deleteEmployee(employeeId);

        Optional<AppUser> deletedEmployee = appUserRepository.findById(employeeId);
        assertFalse(deletedEmployee.isPresent());
    }

    @Test
    void testSearchByCity_Success() {
        employeeService.createEmployee(testEmployeeDTO);

        List<AppUser> employees = employeeService.searchByCity("Bengaluru");

        assertNotNull(employees);
        assertTrue(employees.size() > 0);
        assertTrue(employees.stream()
                .anyMatch(e -> e.getAddress() != null && "Bengaluru".equals(e.getAddress().getCity())));
    }

    @Test
    void testSearchByState_Success() {
        employeeService.createEmployee(testEmployeeDTO);

        List<AppUser> employees = employeeService.searchByState("Karnataka");

        assertNotNull(employees);
        assertTrue(employees.stream()
                .anyMatch(e -> e.getAddress() != null && "Karnataka".equals(e.getAddress().getState())));
    }

    @Test
    void testSearchByCountry_Success() {
        employeeService.createEmployee(testEmployeeDTO);

        List<AppUser> employees = employeeService.searchByCountry("India");

        assertNotNull(employees);
        assertTrue(employees.stream()
                .anyMatch(e -> e.getAddress() != null && "India".equals(e.getAddress().getCountry())));
    }

    @Test
    void testSearchByPincode_Success() {
        employeeService.createEmployee(testEmployeeDTO);

        List<AppUser> employees = employeeService.searchByPincode("560001");

        assertNotNull(employees);
        assertTrue(employees.stream()
                .anyMatch(e -> e.getAddress() != null && "560001".equals(e.getAddress().getPincode())));
    }

    @Test
    void testSearchBySalaryAbove_Success() {
        employeeService.createEmployee(testEmployeeDTO);

        List<AppUser> employees = employeeService.searchBySalaryAbove(40000.0);

        assertNotNull(employees);
        assertTrue(employees.stream()
                .anyMatch(e -> e.getSalary() > 40000.0));
    }

    @Test
    void testSearchBySalaryBelow_Success() {
        employeeService.createEmployee(testEmployeeDTO);

        List<AppUser> employees = employeeService.searchBySalaryBelow(60000.0);

        assertNotNull(employees);
        assertTrue(employees.stream()
                .anyMatch(e -> e.getSalary() < 60000.0));
    }
}

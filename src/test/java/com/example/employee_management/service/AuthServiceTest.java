package com.example.employee_management.service;

import com.example.employee_management.dto.AddressDTO;
import com.example.employee_management.model.AppUser;
import com.example.employee_management.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();

        testUser = new AppUser();
        testUser.setUsername("testuser");
        testUser.setPassword("testpass123");
        testUser.setEmail("testuser@example.com");
        testUser.setSalary(50000.0);
    }

    @Test
    void testRegisterUser_Success() {
        AppUser registeredUser = authService.register(testUser, "ROLE_EMPLOYEE");

        assertNotNull(registeredUser);
        assertNotNull(registeredUser.getId());
        assertEquals("testuser", registeredUser.getUsername());
        assertEquals("testuser@example.com", registeredUser.getEmail());
        assertTrue(registeredUser.getRoles().size() > 0);
    }

    @Test
    void testRegisterUser_PasswordEncrypted() {
        String plainPassword = "testpass123";
        testUser.setPassword(plainPassword);
        
        AppUser registeredUser = authService.register(testUser, "ROLE_EMPLOYEE");

        assertNotNull(registeredUser);
        assertNotEquals(plainPassword, registeredUser.getPassword());
        assertTrue(passwordEncoder.matches(plainPassword, registeredUser.getPassword()));
    }

    @Test
    void testRegisterUser_WithAdminRole() {
        AppUser registeredUser = authService.register(testUser, "ROLE_ADMIN");

        assertNotNull(registeredUser);
        assertTrue(registeredUser.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getName())));
    }

    @Test
    void testRegisterUser_WithManagerRole() {
        AppUser registeredUser = authService.register(testUser, "ROLE_MANAGER");

        assertNotNull(registeredUser);
        assertTrue(registeredUser.getRoles().stream()
                .anyMatch(r -> "ROLE_MANAGER".equals(r.getName())));
    }

    @Test
    void testRegisterUser_DuplicateUsername() {
        authService.register(testUser, "ROLE_EMPLOYEE");

        AppUser duplicateUser = new AppUser();
        duplicateUser.setUsername("testuser");
        duplicateUser.setPassword("differentpass");
        duplicateUser.setEmail("different@example.com");

        assertThrows(RuntimeException.class, () -> authService.register(duplicateUser, "ROLE_EMPLOYEE"));
    }
}

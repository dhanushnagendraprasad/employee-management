package com.example.employee_management.controller;

import com.example.employee_management.dto.EmployeeDTO;
import com.example.employee_management.model.AppUser;
import com.example.employee_management.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // --- STANDARD CRUD ---

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUser> createEmployee(@RequestBody EmployeeDTO dto) {
        return ResponseEntity.status(201).body(service.createEmployee(dto));
    }

    // UPDATED: Now supports ?page=0&size=5&sortBy=salary
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<AppUser>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(service.getAllEmployees(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppUser> getEmployeeById(@PathVariable Long id) {
        AppUser e = service.getEmployeeById(id);
        return (e == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(e);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AppUser> updateEmployee(@PathVariable Long id, @RequestBody AppUser updatedEmployee) {
        try {
            return ResponseEntity.ok(service.updateEmployee(id, updatedEmployee));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/salary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AppUser> applyHike(@PathVariable Long id, @RequestParam double hikePercentage) {
        try {
            return ResponseEntity.ok(service.applySalaryHike(id, hikePercentage));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        service.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // --- SEARCH ENDPOINTS ---

    @GetMapping("/search/city/{city}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AppUser>> searchByCity(@PathVariable String city) {
        return ResponseEntity.ok(service.searchByCity(city));
    }

    @GetMapping("/search/state/{state}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AppUser>> searchByState(@PathVariable String state) {
        return ResponseEntity.ok(service.searchByState(state));
    }

    @GetMapping("/search/country/{country}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AppUser>> searchByCountry(@PathVariable String country) {
        return ResponseEntity.ok(service.searchByCountry(country));
    }

    @GetMapping("/search/pincode/{pincode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AppUser>> searchByPincode(@PathVariable String pincode) {
        return ResponseEntity.ok(service.searchByPincode(pincode));
    }

    @GetMapping("/search/role/{roleName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AppUser>> searchByRole(@PathVariable String roleName) {
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName.toUpperCase();
        }
        return ResponseEntity.ok(service.searchByRole(roleName));
    }

    @GetMapping("/search/salary/above/{amount}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AppUser>> searchBySalaryAbove(@PathVariable Double amount) {
        return ResponseEntity.ok(service.searchBySalaryAbove(amount));
    }
    
    @GetMapping("/search/salary/below/{amount}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AppUser>> searchBySalaryBelow(@PathVariable Double amount) {
        return ResponseEntity.ok(service.searchBySalaryBelow(amount));
    }
}
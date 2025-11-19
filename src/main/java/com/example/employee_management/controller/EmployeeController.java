package com.example.employee_management.controller;

import com.example.employee_management.dto.EmployeeDTO;
import com.example.employee_management.model.AppUser;
import com.example.employee_management.service.EmployeeService;
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

    // Create - Only ADMIN can create new employees via this portal
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUser> createEmployee(@RequestBody EmployeeDTO dto) {
        return ResponseEntity.status(201).body(service.createEmployee(dto));
    }

    // Get All - ADMIN or MANAGER
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AppUser>> getAllEmployees() {
        return ResponseEntity.ok(service.getAllEmployees());
    }

    // Get By ID - Open to Authenticated Users (You can refine this later)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppUser> getEmployeeById(@PathVariable Long id) {
        AppUser e = service.getEmployeeById(id);
        if (e == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(e);
    }

    // Update - ADMIN or MANAGER
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AppUser> updateEmployee(@PathVariable Long id, @RequestBody AppUser updatedEmployee) {
        try {
            return ResponseEntity.ok(service.updateEmployee(id, updatedEmployee));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Salary Hike - ADMIN or MANAGER
    @PutMapping("/{id}/salary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AppUser> applyHike(@PathVariable Long id, @RequestParam double hikePercentage) {
        try {
            return ResponseEntity.ok(service.applySalaryHike(id, hikePercentage));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete - ADMIN ONLY
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        service.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
package com.example.employee_management.controller;

import com.example.employee_management.dto.EmployeeDTO;
import com.example.employee_management.model.Employee;
import com.example.employee_management.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // Create an employee
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeDTO dto) {
        Employee created = service.createEmployee(dto);
        return ResponseEntity.status(201).body(created);
    }

    // Get all employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(service.getAllEmployees());
    }

    // Get by numeric id only (prevents conflicts with other path segments)
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee e = service.getEmployeeById(id);
        if (e == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(e);
    }

    // Update entire employee (by numeric id)
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee updatedEmployee) {

        Employee existing = service.getEmployeeById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        existing.setEmail(updatedEmployee.getEmail());
        existing.setRole(updatedEmployee.getRole());
        existing.setSalary(updatedEmployee.getSalary());
        existing.setAddress(updatedEmployee.getAddress());

        Employee saved = service.saveEmployee(existing);
        return ResponseEntity.ok(saved);
    }

    // Apply a salary hike (PUT) : example -> /api/employees/3/salary?hikePercentage=10
    @PutMapping("/{id:\\d+}/salary")
    public ResponseEntity<Employee> applyHike(@PathVariable Long id,
                                              @RequestParam double hikePercentage) {
        try {
            Employee updated = service.applySalaryHike(id, hikePercentage);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete employee by numeric id
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        service.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // Search by role
    @GetMapping("/search/role/{role}")
    public ResponseEntity<List<Employee>> searchByRole(@PathVariable String role) {
        return ResponseEntity.ok(service.searchByRole(role));
    }

    // Search by city
    @GetMapping("/search/city/{city}")
    public ResponseEntity<List<Employee>> searchByCity(@PathVariable String city) {
        return ResponseEntity.ok(service.searchByCity(city));
    }

    // Salary above
    @GetMapping("/salary/above/{amount}")
    public ResponseEntity<List<Employee>> findBySalaryAbove(@PathVariable double amount) {
        return ResponseEntity.ok(service.findBySalaryAbove(amount));
    }

    // Count
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCount() {
        return ResponseEntity.ok(service.getTotalCount());
    }
}

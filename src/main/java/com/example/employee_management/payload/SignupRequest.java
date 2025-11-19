package com.example.employee_management.payload;

import com.example.employee_management.model.Address;

public class SignupRequest {
    private String username;
    private String password;
    private String email;      // Added
    private String role;       // e.g. "ROLE_MANAGER"
    private Double salary;     // Added
    private Address address;   // Added

    // --- Getters & Setters ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}
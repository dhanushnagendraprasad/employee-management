package com.example.employee_management.dto;

public class EmployeeDTO {
    private String email;
    private String role;
    private double salary;
    private AddressDTO address; // ✅ use AddressDTO, not String

    public EmployeeDTO() {}

    public EmployeeDTO(String email, String role, double salary, AddressDTO address) {
        this.email = email;
        this.role = role;
        this.salary = salary;
        this.address = address;
    }

    // Getters & setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }
}

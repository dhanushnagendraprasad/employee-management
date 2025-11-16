package com.example.employee_management.service;

import com.example.employee_management.dto.EmployeeDTO;
import com.example.employee_management.model.Employee;
import com.example.employee_management.model.Address;  // ✅ ADD THIS IMPORT
import com.example.employee_management.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public Employee createEmployee(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setEmail(dto.getEmail());
        employee.setRole(dto.getRole());
        employee.setSalary(dto.getSalary()); // ✅ dto must have getSalary()

        // ✅ Build Address object properly
        Address address = new Address();
        address.setCity(dto.getAddress().getCity());
        address.setState(dto.getAddress().getState());
        address.setCountry(dto.getAddress().getCountry());
        address.setPincode(dto.getAddress().getPincode());
        employee.setAddress(address);

        return repository.save(employee);
    }

    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    public Employee applySalaryHike(Long id, double hikePercentage) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found!"));
        double newSalary = employee.getSalary() + (employee.getSalary() * hikePercentage / 100);
        employee.setSalary(newSalary);
        return repository.save(employee);
    }

    public void deleteEmployee(Long id) {
        repository.deleteById(id);
    }

    public Employee saveEmployee(Employee employee) {
        return repository.save(employee);
    }

    public Employee getEmployeeById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Employee> searchByRole(String role) {
        return repository.findByRole(role);
    }

    public List<Employee> searchByCity(String city) {
        return repository.findByAddress_City(city);
    }

    public List<Employee> findBySalaryAbove(double amount) {
        return repository.findBySalaryGreaterThan(amount);
    }

    public long getTotalCount() {
        return repository.count();
    }
}

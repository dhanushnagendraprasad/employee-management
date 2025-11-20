package com.example.employee_management.service;

import com.example.employee_management.dto.EmployeeDTO;
import com.example.employee_management.model.Address;
import com.example.employee_management.model.AppUser;
import com.example.employee_management.model.Role;
import com.example.employee_management.repository.AppUserRepository;
import com.example.employee_management.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public class EmployeeService {

    private final AppUserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(AppUserRepository repository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- CRUD METHODS ---

    @Transactional
    public AppUser createEmployee(EmployeeDTO dto) {
        AppUser user = new AppUser();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setSalary(dto.getSalary() == null ? 400000.0 : dto.getSalary());
        
        String rawPassword = dto.getPassword() != null ? dto.getPassword() : "default123";
        user.setPassword(passwordEncoder.encode(rawPassword));

        if (dto.getAddress() != null) {
            Address address = new Address();
            address.setCity(dto.getAddress().getCity());
            address.setState(dto.getAddress().getState());
            address.setCountry(dto.getAddress().getCountry());
            address.setPincode(dto.getAddress().getPincode());
            user.setAddress(address);
        }

        String roleName = dto.getRole() != null ? dto.getRole() : "ROLE_EMPLOYEE";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        return repository.save(user);
    }

    // UPDATED: Pagination and Sorting support
    public Page<AppUser> getAllEmployees(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAll(pageable);
    }
    
    public AppUser getEmployeeById(Long id) { return repository.findById(id).orElse(null); }

    @Transactional
    public AppUser updateEmployee(Long id, AppUser updatedData) {
        AppUser existing = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if(updatedData.getEmail() != null) existing.setEmail(updatedData.getEmail());
        if(updatedData.getSalary() != null) existing.setSalary(updatedData.getSalary());
        if (updatedData.getAddress() != null && existing.getAddress() != null) {
            existing.getAddress().setCity(updatedData.getAddress().getCity());
            existing.getAddress().setState(updatedData.getAddress().getState());
            existing.getAddress().setCountry(updatedData.getAddress().getCountry());
            existing.getAddress().setPincode(updatedData.getAddress().getPincode());
        }
        return repository.save(existing);
    }

    @Transactional
    public AppUser applySalaryHike(Long id, double hikePercentage) {
        AppUser user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        double newSalary = user.getSalary() + (user.getSalary() * hikePercentage / 100.0);
        user.setSalary(newSalary);
        return repository.save(user);
    }

    public void deleteEmployee(Long id) { repository.deleteById(id); }

    // --- SEARCH METHODS (FILTERS) ---
    public List<AppUser> searchByCity(String city) { return repository.findByAddress_City(city); }
    public List<AppUser> searchByState(String state) { return repository.findByAddress_State(state); }
    public List<AppUser> searchByCountry(String country) { return repository.findByAddress_Country(country); }
    public List<AppUser> searchByPincode(String pincode) { return repository.findByAddress_Pincode(pincode); }
    public List<AppUser> searchByRole(String roleName) { return repository.findByRoles_Name(roleName); }
    public List<AppUser> searchBySalaryAbove(Double salary) { return repository.findBySalaryGreaterThanEqual(salary); }
    public List<AppUser> searchBySalaryBelow(Double salary) { return repository.findBySalaryLessThanEqual(salary); }
}
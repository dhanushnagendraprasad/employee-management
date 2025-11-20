package com.example.employee_management.repository;

import com.example.employee_management.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    // Auth Basics
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);

    // --- 1. Geography Filters (Traversing Address Entity) ---
    List<AppUser> findByAddress_City(String city);
    List<AppUser> findByAddress_State(String state);
    List<AppUser> findByAddress_Country(String country);
    List<AppUser> findByAddress_Pincode(String pincode);

    // --- 2. Financial Filters ---
    List<AppUser> findBySalaryGreaterThanEqual(Double salary);
    List<AppUser> findBySalaryLessThanEqual(Double salary);

    // --- 3. Role Filters (Traversing Roles Entity) ---
    // This finds users who have a specific role (e.g., "ROLE_MANAGER")
    List<AppUser> findByRoles_Name(String roleName);
}
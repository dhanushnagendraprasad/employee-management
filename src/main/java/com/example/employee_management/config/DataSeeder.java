package com.example.employee_management.config;

import com.example.employee_management.model.Role;
import com.example.employee_management.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if roles exist, if not, create them
        if (roleRepository.count() == 0) {
            Role admin = new Role("ROLE_ADMIN");
            Role manager = new Role("ROLE_MANAGER");
            Role employee = new Role("ROLE_EMPLOYEE");
            
            roleRepository.saveAll(Arrays.asList(admin, manager, employee));
            System.out.println("--- DataSeeder: Default roles inserted into DB ---");
        }
    }
}
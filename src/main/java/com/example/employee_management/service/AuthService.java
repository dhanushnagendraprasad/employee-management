package com.example.employee_management.service;

import com.example.employee_management.model.AppUser;
import com.example.employee_management.model.Role;
import com.example.employee_management.repository.AppUserRepository;
import com.example.employee_management.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    public AuthService(AppUserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    public AppUser register(AppUser user, String roleName) {
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username exists");
        }

        System.out.println(">>> [REGISTER] Raw Password: " + user.getPassword());
        
        // Encrypt the password before saving
        user.setPassword(encoder.encode(user.getPassword()));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.setRoles(new HashSet<>(Collections.singletonList(role)));
        
        AppUser savedUser = userRepository.save(user);
        System.out.println(">>> [REGISTER] User Saved. ID: " + savedUser.getId());
        return savedUser;
    }
}
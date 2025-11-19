package com.example.employee_management.service;

import com.example.employee_management.model.AppUser;
import com.example.employee_management.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository repository;

    // Constructor injection of the NEW repository
    public CustomUserDetailsService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(">>> [LOGIN] Searching 'app_users' table for: " + username);

        AppUser user = repository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println(">>> [LOGIN] User NOT found in 'app_users'");
                    return new UsernameNotFoundException("User not found");
                });

        System.out.println(">>> [LOGIN] Found User: " + user.getUsername());
        System.out.println(">>> [LOGIN] DB Password Hash: " + user.getPassword());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList())
        );
    }
}
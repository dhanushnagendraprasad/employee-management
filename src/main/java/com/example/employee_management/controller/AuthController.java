package com.example.employee_management.controller;

import com.example.employee_management.model.User;
import com.example.employee_management.payload.LoginRequest;
import com.example.employee_management.payload.SignupRequest;
import com.example.employee_management.payload.JwtResponse;
import com.example.employee_management.repository.UserRepository;
import com.example.employee_management.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signup) {
        if (userRepository.existsByUsername(signup.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        User u = new User();
        u.setUsername(signup.getUsername());
        u.setPassword(passwordEncoder.encode(signup.getPassword()));
        u.setRole(signup.getRole() == null ? "EMPLOYEE" : signup.getRole().toUpperCase());
        userRepository.save(u);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenUtil.generateToken(authentication);
        return ResponseEntity.ok(new JwtResponse(jwt, authentication.getName()));
    }
}

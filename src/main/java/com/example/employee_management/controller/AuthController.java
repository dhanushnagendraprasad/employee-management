package com.example.employee_management.controller;

import com.example.employee_management.model.AppUser;
import com.example.employee_management.payload.JwtResponse;
import com.example.employee_management.payload.LoginRequest;
import com.example.employee_management.payload.SignupRequest; // Import this
import com.example.employee_management.security.JwtTokenUtil;
import com.example.employee_management.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthController(AuthenticationManager authenticationManager, AuthService authService, JwtTokenUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SignupRequest req) {
        // 1. Create Entity from DTO
        AppUser user = new AppUser();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setEmail(req.getEmail());
        user.setSalary(req.getSalary());
        user.setAddress(req.getAddress());

        // 2. Determine Role (Use input, or default to EMPLOYEE)
        String roleToAssign = (req.getRole() != null && !req.getRole().isEmpty()) 
                              ? req.getRole() 
                              : "ROLE_EMPLOYEE";

        // 3. Register
        return ResponseEntity.ok(authService.register(user, roleToAssign));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        
        String token = jwtTokenUtil.generateToken(req.getUsername(), authentication.getAuthorities());
        return ResponseEntity.ok(new JwtResponse(token, req.getUsername()));
    }
}
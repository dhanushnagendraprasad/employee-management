package com.example.employee_management.controller;

import com.example.employee_management.model.AppUser;
import com.example.employee_management.repository.AppUserRepository;
import com.example.employee_management.payload.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final AppUserRepository repo;
    private final PasswordEncoder encoder;

    public TestController(AppUserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @PostMapping("/verify")
    public String verifyPassword(@RequestBody LoginRequest req) {
        AppUser user = repo.findByUsername(req.getUsername()).orElse(null);
        if (user == null) return "User NOT FOUND in database.";

        boolean matches = encoder.matches(req.getPassword(), user.getPassword());

        return "DEBUG REPORT:\n" +
               "------------------------------------------------\n" +
               "Username      : " + user.getUsername() + "\n" +
               "Input Pwd     : " + req.getPassword() + "\n" +
               "Stored Hash   : " + user.getPassword() + "\n" +
               "Matches?      : " + matches + "\n" +
               "------------------------------------------------";
    }
}
package com.reposcribe.controller;

import com.reposcribe.dto.AuthResponse;
import com.reposcribe.dto.LoginRequest;
import com.reposcribe.dto.RegisterRequest;
import com.reposcribe.model.User;
import com.reposcribe.service.JwtService;
import com.reposcribe.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(null, null, "Username is required"));
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(null, null, "Password must be at least 6 characters"));
        }

        // Try to register user
        User user = userService.register(request.getUsername(), request.getPassword());
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthResponse(null, null, "Username already exists"));
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getUsername());
        
        return ResponseEntity.ok(
            new AuthResponse(token, user.getUsername(), "Registration successful")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                .body(new AuthResponse(null, null, "Username and password are required"));
        }

        // Authenticate user
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(null, null, "Invalid username or password"));
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getUsername());
        
        return ResponseEntity.ok(
            new AuthResponse(token, user.getUsername(), "Login successful")
        );
    }
}


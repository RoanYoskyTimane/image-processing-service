package com.roanyosky.image_processing_service.controllers;

import com.roanyosky.image_processing_service.entities.User;
import com.roanyosky.image_processing_service.repositories.UserRepository;
import com.roanyosky.image_processing_service.services.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        // In a real app, use AuthenticationManager to verify credentials
        User foundUser = userRepository.findByUsername(user.getUsername());

        if (passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            String token = jwtService.generateToken((UserDetails) foundUser);
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}

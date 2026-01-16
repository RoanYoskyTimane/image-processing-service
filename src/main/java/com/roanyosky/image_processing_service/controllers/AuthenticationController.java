package com.roanyosky.image_processing_service.controllers;

import com.roanyosky.image_processing_service.dtos.AuthenticationResponseDto;
import com.roanyosky.image_processing_service.dtos.LoginDto;
import com.roanyosky.image_processing_service.dtos.UserCreateDto;
import com.roanyosky.image_processing_service.repositories.UserRepository;
import com.roanyosky.image_processing_service.services.AuthenticationService;
import com.roanyosky.image_processing_service.services.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(@RequestBody UserCreateDto userCreateDto) {
        AuthenticationResponseDto response = authenticationService.register(userCreateDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@RequestBody LoginDto loginDto) {
        AuthenticationResponseDto response = authenticationService.autheticate(loginDto);

        return ResponseEntity.ok(response);
    }
}

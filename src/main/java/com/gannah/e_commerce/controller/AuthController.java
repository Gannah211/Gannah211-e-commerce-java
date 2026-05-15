package com.gannah.e_commerce.controller;

import com.gannah.e_commerce.DTO.request.LoginRequest;
import com.gannah.e_commerce.DTO.request.RegisterRequest;
import com.gannah.e_commerce.DTO.response.AuthResponse;
import com.gannah.e_commerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest Request) {
        return ResponseEntity.ok(authService.register(Request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest Request) {
        return ResponseEntity.ok(authService.login(Request));
    }
}

package com.shop.auth;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shop.security.JwtService;
import com.shop.user.User;
import com.shop.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users,
                       PasswordEncoder encoder,
                       JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.AuthResponse register(
            AuthDtos.RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(encoder.encode(request.password()));
        user.setRole("CUSTOMER");

        users.save(user);

        return new AuthDtos.AuthResponse(
            jwtService.generateToken(user.getEmail(), user.getRole()),
            user.getEmail(),
            user.getRole()
        );
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = users.findByEmail(
            request.email().trim().toLowerCase()
        ).orElseThrow(() ->
            new IllegalArgumentException("Invalid email or password"));

        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException(
                "Invalid email or password");
        }

        return new AuthDtos.AuthResponse(
            jwtService.generateToken(user.getEmail(), user.getRole()),
            user.getEmail(),
            user.getRole()
        );
    }
}
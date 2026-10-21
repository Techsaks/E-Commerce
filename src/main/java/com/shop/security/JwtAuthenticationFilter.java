package com.shop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtService.isValid(token)) {
                    String email = jwtService.extractEmail(token);
                    var authorities = java.util.List.of(
                        new SimpleGrantedAuthority("ROLE_" +
                            io.jsonwebtoken.Jwts.parser()
                                .verifyWith(
                                    io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                                        System.getenv().getOrDefault(
                                            "JWT_SECRET",
                                            "AssessmentSecretKeyMustBeAtLeast32Bytes!"
                                        ).getBytes(java.nio.charset.StandardCharsets.UTF_8)
                                    )
                                ).build().parseSignedClaims(token)
                                .getPayload().get("role", String.class))
                    );

                    var authentication =
                        new UsernamePasswordAuthenticationToken(
                            email, null, authorities);

                    SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
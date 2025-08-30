package com.aritra.d.riad.CoWork.security;

import com.aritra.d.riad.CoWork.model.Users;
import com.aritra.d.riad.CoWork.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter implements Filter {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private UserService userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (validateToken(token)) {
                    Claims claims = parseToken(token);
                    String userId = claims.getSubject();
                    String email = claims.get("email", String.class);
                    
                    // Sync user from JWT to database
                    Users user = userService.syncUserFromJWT(claims);
                    
                    // Create Spring Security authorities from user roles
                    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                        .collect(Collectors.toList());
                    
                    // Set authentication in Spring Security context
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // Add user info as request attributes for easy access in controllers
                    httpRequest.setAttribute("userId", userId);
                    httpRequest.setAttribute("userEmail", email);
                    httpRequest.setAttribute("userClaims", claims);
                    httpRequest.setAttribute("authenticatedUser", user);
                    httpRequest.setAttribute("authenticated", true);
                } else {
                    httpRequest.setAttribute("authenticated", false);
                }
            } else {
                httpRequest.setAttribute("authenticated", false);
            }
        } catch (Exception e) {
            System.err.println("JWT Authentication error: " + e.getMessage());
            httpRequest.setAttribute("authenticated", false);
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            return false;
        }
    }
    
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}

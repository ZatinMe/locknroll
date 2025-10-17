package com.locknroll.controller;

import com.locknroll.dto.LoginRequest;
import com.locknroll.dto.LoginResponse;
import com.locknroll.dto.RegisterRequest;
import com.locknroll.dto.UserDto;
import com.locknroll.entity.User;
import com.locknroll.security.CustomUserDetailsService;
import com.locknroll.security.JwtTokenProvider;
import com.locknroll.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for authentication operations
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getUsernameOrEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            List<String> roles = userPrincipal.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

            LoginResponse response = new LoginResponse();
            response.setAccessToken(jwt);
            response.setTokenType("Bearer");
            response.setExpiresIn(tokenProvider.getExpirationInMs());
            response.setUsername(userPrincipal.getUsername());
            response.setEmail(userPrincipal.getEmail());
            response.setFullName(userPrincipal.getFullName());
            response.setRoles(roles);

            logger.info("Successful login for user: {}", loginRequest.getUsernameOrEmail());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Login failed for user: {}", loginRequest.getUsernameOrEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Registration attempt for user: {}", registerRequest.getUsername());
        
        try {
            UserDto userDto = new UserDto();
            userDto.setUsername(registerRequest.getUsername());
            userDto.setEmail(registerRequest.getEmail());
            // Password will be set by the service
            userDto.setFirstName(registerRequest.getFirstName());
            userDto.setLastName(registerRequest.getLastName());
            userDto.setPhoneNumber(registerRequest.getPhoneNumber());

            UserDto createdUser = userService.createUser(userDto, registerRequest.getPassword());
            
            logger.info("Successful registration for user: {}", registerRequest.getUsername());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
            
        } catch (Exception e) {
            logger.error("Registration failed for user: {}", registerRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            
            UserDto userDto = userService.getUserById(userPrincipal.getUserId());
            return ResponseEntity.ok(userDto);
            
        } catch (Exception e) {
            logger.error("Failed to get current user", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken() {
        try {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            
            String jwt = tokenProvider.generateTokenFromUsername(userPrincipal.getUsername());
            
            List<String> roles = userPrincipal.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

            LoginResponse response = new LoginResponse();
            response.setAccessToken(jwt);
            response.setTokenType("Bearer");
            response.setExpiresIn(tokenProvider.getExpirationInMs());
            response.setUsername(userPrincipal.getUsername());
            response.setEmail(userPrincipal.getEmail());
            response.setFullName(userPrincipal.getFullName());
            response.setRoles(roles);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to refresh token", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

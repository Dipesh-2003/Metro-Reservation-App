package com.aurionpro.app.controller;

import com.aurionpro.app.dto.*;
import com.aurionpro.app.security.JwtService;
import com.aurionpro.app.service.OtpService;
import com.aurionpro.app.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication", description = "APIs for user registration and login")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final OtpService otpService;

    @PostMapping("/register")
    @Operation(summary = "1. Register a new user and send verification OTP", description = "Creates a new, disabled user and sends an OTP to their email for account verification.")
    public ResponseEntity<String> registerUser(@RequestBody SignUpRequest signUpRequest) {
        userService.registerUser(signUpRequest);
        return ResponseEntity.ok("Registration successful. Please check your email for the verification OTP.");
    }


    @PostMapping("/admin/login")
    @Operation(summary = "Login for administrators", description = "Authenticates a user and returns a JWT if they have the ADMIN role.")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest loginRequest) {
        // Step 1: Authenticate the admin's credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (!isAdmin) {
            // If not an admin, deny access
            return new ResponseEntity<>("Access Denied: Not an authorized administrator.", HttpStatus.FORBIDDEN);
        }

        // Step 4: Generate and return the JWT
        final String jwtToken = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(jwtToken));
    }
    
//    @PostMapping("/send-otp")
//    @Operation(summary = "Send OTP to user's email", description = "Generates and sends a 6-digit OTP to the provided email for login or registration.")
//    public ResponseEntity<String> sendOtp(@RequestBody OtpRequestDto otpRequest) {
//        otpService.sendOtp(otpRequest.getEmail());
//        return ResponseEntity.ok("OTP sent successfully to " + otpRequest.getEmail());
//    }

    @PostMapping("/verify-otp")
    @Operation(summary = "2. Verify registration OTP", description = "Verifies the OTP sent during registration to activate the user's account.")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequestDto verifyRequest) {
        otpService.verifyOtp(verifyRequest);
        return ResponseEntity.ok("Account verified successfully. You may now log in.");
    }

    @PostMapping("/user/login")
    @Operation(summary = "Login for regular users and staff", description = "Authenticates a user and returns a JWT if they have the USER role.")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
    	authenticationManager.authenticate(
    			new UsernamePasswordAuthenticationToken(
    					loginRequest.getEmail(),
    					loginRequest.getPassword()
    					)
    			);
    	
    	final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
    	
    	boolean isUser = userDetails.getAuthorities().stream()
    			.map(GrantedAuthority::getAuthority)
    			.anyMatch(role -> role.equals("ROLE_USER") || role.equals("ROLE_STAFF"));
    	
    	if (!isUser) {
    		return new ResponseEntity<>("Access Denied: This login is for users only.", HttpStatus.FORBIDDEN);
    	}
    	
    	final String jwtToken = jwtService.generateToken(userDetails);
    	return ResponseEntity.ok(new JwtResponse(jwtToken));
    }
}
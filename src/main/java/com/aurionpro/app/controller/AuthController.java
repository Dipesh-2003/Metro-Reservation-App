package com.aurionpro.app.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.app.dto.JwtResponse;
import com.aurionpro.app.dto.LoginRequest;
import com.aurionpro.app.dto.OtpRequestDto;
import com.aurionpro.app.dto.RefreshTokenRequest;
import com.aurionpro.app.dto.ResetPasswordRequest;
import com.aurionpro.app.dto.SignUpRequest;
import com.aurionpro.app.dto.VerifyOtpRequestDto;
import com.aurionpro.app.entity.RefreshToken;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.security.JwtService;
import com.aurionpro.app.service.OtpService;
import com.aurionpro.app.service.RefreshTokenService;
import com.aurionpro.app.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
    private final RefreshTokenService refreshTokenService;

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
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken((User) userDetails);

        return ResponseEntity.ok(JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build());
    }
    
//    @PostMapping("/send-otp")
//    @Operation(summary = "Send OTP to user's email", description = "Generates and sends a 6-digit OTP to the provided email for login or registration.")
//    public ResponseEntity<String> sendOtp(@RequestBody OtpRequestDto otpRequest) {
//        otpService.sendOtp(otpRequest.getEmail());
//        return ResponseEntity.ok("OTP sent successfully to " + otpRequest.getEmail());
//    }

    @PostMapping("/verify-otp")
    @Operation(summary = "2. Verify registration OTP and get tokens", description = "Verifies the OTP to activate the account and returns access and refresh tokens.")
    public ResponseEntity<JwtResponse> verifyOtp(@RequestBody VerifyOtpRequestDto verifyRequest) { // <-- Change return type
        JwtResponse jwtResponse = otpService.verifyOtp(verifyRequest);
        return ResponseEntity.ok(jwtResponse);
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
    	String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken((User) userDetails);

        return ResponseEntity.ok(JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build());

    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token.")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        //verify the refresh token
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        
        //generate a new access token for the user associated with the refresh token
        String newAccessToken = jwtService.generateToken(refreshToken.getUser());
        
        return ResponseEntity.ok(JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .build());
    }

    //logout endpoint
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revokes the user's refresh token, effectively logging them out.")
    @SecurityRequirement(name = "bearerAuth") //requires an access token to identify the user
    public ResponseEntity<String> logout(Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        refreshTokenService.deleteRefreshToken(currentUser);
        return ResponseEntity.ok("User logged out successfully.");
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Step 1: Request a password reset OTP", description = "Sends an OTP to the user's email if the account exists.")
    public ResponseEntity<String> forgotPassword(@RequestBody OtpRequestDto request) {
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("An OTP has been sent to your email address to reset your password.");
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Step 2: Verify OTP and set a new password", description = "Verifies the OTP and updates the user's password to the new one provided.")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok("Your password has been reset successfully.");
    }
}

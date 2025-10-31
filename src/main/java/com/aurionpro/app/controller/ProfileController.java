package com.aurionpro.app.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping; // <-- Make sure this is added
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.app.dto.CloudinarySignatureResponse;
import com.aurionpro.app.dto.DeleteAccountRequest;
import com.aurionpro.app.dto.UpdateProfileImageRequest;
import com.aurionpro.app.dto.UserDto;
import com.aurionpro.app.entity.User; // <-- Add this import
import com.aurionpro.app.mapper.UserMapper; // <-- Add this import
import com.aurionpro.app.service.FileUploadService;
import com.aurionpro.app.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "5. Profile Management", description = "APIs for managing user profiles")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final FileUploadService fileUploadService;
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user's profile")
    public ResponseEntity<UserDto> getMyProfile(Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        return ResponseEntity.ok(userMapper.entityToDto(currentUser));
    }

    @PostMapping("/upload/signature")
    @Operation(summary = "Get a signature for direct Cloudinary upload (Step 1)", description = "Generates a temporary, secure signature that the client can use to upload a file directly to Cloudinary.")
    public ResponseEntity<CloudinarySignatureResponse> getUploadSignature() {
        return ResponseEntity.ok(fileUploadService.generateUploadSignature());
    }

    @PostMapping("/update-image-url")
    @Operation(summary = "Update user's profile image URL (Step 2)", description = "After a successful direct upload to Cloudinary, the client sends the resulting URL to this endpoint to save it.")
    public ResponseEntity<UserDto> updateProfileImageUrl(@RequestBody UpdateProfileImageRequest request, Principal principal) {
        UserDto updatedUser = userService.updateUserProfileImage(principal.getName(), request.getImageUrl());
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/me")
    @Operation(summary = "Delete the authenticated user's account", description = "Permanently deletes the user's account and all associated data. This action is irreversible and requires password confirmation.")
    public ResponseEntity<String> deleteAccount(@RequestBody DeleteAccountRequest request, Principal principal) {
        userService.deleteUserAccount(principal.getName(), request.getPassword());
        return ResponseEntity.ok("Your account has been successfully deleted.");
    }
}
// Create New File: src/main/java/com/aurionpro/app/controller/ProfileController.java

package com.aurionpro.app.controller;

import com.aurionpro.app.dto.UserDto;
import com.aurionpro.app.service.FileUploadService;
import com.aurionpro.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "5. Profile Management", description = "APIs for managing user profiles")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final FileUploadService fileUploadService;
    private final UserService userService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or update profile picture", description = "Uploads an image file to Cloudinary and updates the authenticated user's profile image URL.")
    public ResponseEntity<UserDto> uploadProfileImage(@RequestParam("file") MultipartFile file, Principal principal) {
        try {
            // Check if the file is empty
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Upload the file and get the URL
            String imageUrl = fileUploadService.uploadFile(file);

            // Update the user's profile with the new image URL
            UserDto updatedUser = userService.updateUserProfileImage(principal.getName(), imageUrl);

            return ResponseEntity.ok(updatedUser);

        } catch (IOException e) {
            // Handle exceptions during file upload
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
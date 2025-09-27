package com.aurionpro.app.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.app.dto.ScanRequestDto;
import com.aurionpro.app.dto.ValidationHistoryDto;
import com.aurionpro.app.dto.ValidationResponseDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.service.UserService;
import com.aurionpro.app.service.ValidationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/validate")
@RequiredArgsConstructor
@Tag(name = "6. Ticket Validation (Staff)", description = "APIs for staff to validate tickets")
public class ValidationController {

    private final ValidationService validationService;
    private final UserService userService;

    @PostMapping("/scan")
    @PreAuthorize("hasRole('STAFF')")
    @Operation(summary = "Validate a ticket by QR code payload (Staff only)", description = "Accepts the raw JSON payload from a scanned QR code and returns the validation result.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ValidationResponseDto> scanTicket(
            @RequestBody ScanRequestDto scanRequest,
            Principal principal) {
        
        //get the currently authenticated staff member
        User staffMember = userService.findUserEntityByEmail(principal.getName());

        //call the service to perform the validation
        ValidationResponseDto response = validationService.validateTicket(scanRequest, staffMember);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasRole('STAFF')")
    @Operation(summary = "Get validation history for a ticket (Staff only)", description = "Retrieves a list of all validation attempts for a specific ticket ID.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<ValidationHistoryDto>> getHistory(@RequestParam Integer ticketId) {
        List<ValidationHistoryDto> history = validationService.getValidationHistory(ticketId);
        return ResponseEntity.ok(history);
    }
}
// In: com/aurionpro/app/controller/AdminController.java
package com.aurionpro.app.controller;

import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.StationDto;
import com.aurionpro.app.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "2. Admin Management", description = "APIs for managing stations, fares, etc.")
public class AdminController {

    private final StationService stationService;

    @PostMapping("/stations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Add a new station (Admin only)",
        description = "Creates a new metro station in the system. Requires ADMIN role."
    )
    @SecurityRequirement(name = "bearerAuth") 
    public ResponseEntity<StationDto> addStation(@RequestBody CreateStationRequest createRequest) {
        StationDto newStation = stationService.addStation(createRequest);
        return new ResponseEntity<>(newStation, HttpStatus.CREATED);
    }
}
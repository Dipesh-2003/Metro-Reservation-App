package com.aurionpro.app.controller;

import com.aurionpro.app.dto.CreateFareRuleRequest;
import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.FareRuleDto;
import com.aurionpro.app.dto.StationDto;
import com.aurionpro.app.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
    @Operation(summary = "Add a new station (Admin only)", description = "Creates a new metro station in the system.")
    @SecurityRequirement(name = "bearerAuth") 
    public ResponseEntity<StationDto> addStation(@RequestBody CreateStationRequest createRequest) {
        StationDto newStation = stationService.addStation(createRequest);
        return new ResponseEntity<>(newStation, HttpStatus.CREATED);
    }
    
    @PostMapping("/stations/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add multiple new stations (Admin only)", description = "Creates a list of new metro stations in the system from a JSON array.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<StationDto>> addStations(@RequestBody List<CreateStationRequest> createRequests) {
        List<StationDto> newStations = stationService.addStations(createRequests);
        return new ResponseEntity<>(newStations, HttpStatus.CREATED);
    }

    // New Endpoint to add a Fare Rule
    @PostMapping("/fares")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new fare rule (Admin only)", description = "Creates a fare rule for a specific route between two stations.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FareRuleDto> addFareRule(@RequestBody CreateFareRuleRequest fareRuleRequest) {
        FareRuleDto newFareRule = stationService.addFareRule(fareRuleRequest);
        return new ResponseEntity<>(newFareRule, HttpStatus.CREATED);
    }
}
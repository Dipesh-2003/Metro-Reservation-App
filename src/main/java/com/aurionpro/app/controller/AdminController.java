package com.aurionpro.app.controller;

import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.StationDto;
import com.aurionpro.app.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StationService stationService;
    // You would also inject a FareRuleService here later

    @PostMapping("/stations")
    @PreAuthorize("hasRole('ADMIN')") // This is the magic!
    public ResponseEntity<StationDto> addStation(@RequestBody CreateStationRequest createRequest) {
        StationDto newStation = stationService.addStation(createRequest);
        return new ResponseEntity<>(newStation, HttpStatus.CREATED);
    }

    // You can add more admin-only endpoints here, for example:
    // @PostMapping("/fare-rules")
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<?> addFareRule(...) { ... }
}
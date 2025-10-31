package com.aurionpro.app.controller;

import com.aurionpro.app.dto.StationDto;
import com.aurionpro.app.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@Tag(name = "8. Station Information", description = "Public APIs for retrieving station data")
public class StationController {

    private final StationService stationService;

    @GetMapping
    @Operation(summary = "Get all active stations", description = "Retrieves a list of all currently active metro stations. This endpoint is public.")
    public ResponseEntity<List<StationDto>> getAllStations() {
        List<StationDto> stations = stationService.getAllStations();
        return ResponseEntity.ok(stations);
    }
}
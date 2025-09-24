package com.aurionpro.app.service;

import java.util.List;
import com.aurionpro.app.dto.CreateFareRuleRequest;
import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.FareRuleDto;
import com.aurionpro.app.dto.StationDto;

public interface StationService {
    StationDto addStation(CreateStationRequest createRequest);
    
    // New method for adding multiple stations
    List<StationDto> addStations(List<CreateStationRequest> createRequests);

    StationDto getStationById(Integer id);
    List<StationDto> getAllStations();
    void deleteStation(Integer id);
    FareRuleDto addFareRule(CreateFareRuleRequest fareRuleRequest);
}
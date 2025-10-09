package com.aurionpro.app.service;

import java.util.List;
import com.aurionpro.app.dto.CreateFareSlabRequest;
import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.FareSlabDto;
import com.aurionpro.app.dto.StationDto;

public interface StationService {
    StationDto addStation(CreateStationRequest createRequest);
    List<StationDto> addStations(List<CreateStationRequest> createRequests);
    StationDto updateStation(Integer stationId, CreateStationRequest updateRequest);
    StationDto getStationById(Integer id);
    List<StationDto> getAllStations();
    void deleteStation(Integer id);
    FareSlabDto addFareSlab(CreateFareSlabRequest fareSlabRequest);
}
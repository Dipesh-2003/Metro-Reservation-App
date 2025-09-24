package com.aurionpro.app.service;

import java.util.List;

import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.StationDto;

public interface StationService {
    StationDto addStation(CreateStationRequest createRequest);
    StationDto getStationById(Integer id);
    List<StationDto> getAllStations();
    void deleteStation(Integer id);
}
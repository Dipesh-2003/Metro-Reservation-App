package com.aurionpro.app.service.implementation;

import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.StationDto;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.StationMapper; // <-- Import mapper
import com.aurionpro.app.repository.StationRepository;
import com.aurionpro.app.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StationServiceImpl implements StationService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private StationMapper stationMapper; // <-- Inject mapper

    @Override
    public StationDto addStation(CreateStationRequest createRequest) {
        // Convert DTO to entity using the mapper
        Station newStation = stationMapper.dtoToEntity(createRequest);
        Station savedStation = stationRepository.save(newStation);
        // Convert saved entity back to DTO for the response
        return stationMapper.entityToDto(savedStation);
    }

    @Override
    public StationDto getStationById(Integer id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + id));
        // Use mapper for conversion
        return stationMapper.entityToDto(station);
    }

    @Override
    public List<StationDto> getAllStations() {
        List<Station> stations = stationRepository.findAll();
        // Use mapper for list conversion
        return stationMapper.entityToDto(stations);
    }

    @Override
    public void deleteStation(Integer id) {
        if (!stationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Station not found with ID: " + id);
        }
        stationRepository.deleteById(id);
    }
}
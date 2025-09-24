package com.aurionpro.app.service.implementation;

import com.aurionpro.app.dto.CreateFareRuleRequest;
import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.FareRuleDto;
import com.aurionpro.app.dto.StationDto;
import com.aurionpro.app.entity.FareRule;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.FareRuleMapper;
import com.aurionpro.app.mapper.StationMapper;
import com.aurionpro.app.repository.FareRuleRepository;
import com.aurionpro.app.repository.StationRepository;
import com.aurionpro.app.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final FareRuleRepository fareRuleRepository;
    private final StationMapper stationMapper;
    private final FareRuleMapper fareRuleMapper; 

    @Override
    public StationDto addStation(CreateStationRequest createRequest) {
        Station newStation = stationMapper.dtoToEntity(createRequest);
        Station savedStation = stationRepository.save(newStation);
        return stationMapper.entityToDto(savedStation);
    }
    
    @Override
    public List<StationDto> addStations(List<CreateStationRequest> createRequests) {
        // 1. Map the list of DTOs to a list of entities
        List<Station> stationsToSave = createRequests.stream()
                .map(stationMapper::dtoToEntity)
                .collect(Collectors.toList());

        // 2. Save all entities in a single transaction (more efficient)
        List<Station> savedStations = stationRepository.saveAll(stationsToSave);

        // 3. Map the list of saved entities back to a list of DTOs for the response
        return savedStations.stream()
                .map(stationMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public StationDto getStationById(Integer id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + id));
        return stationMapper.entityToDto(station);
    }

    @Override
    public List<StationDto> getAllStations() {
        List<Station> stations = stationRepository.findAll();
        return stationMapper.entityToDto(stations);
    }

    @Override
    public void deleteStation(Integer id) {
        if (!stationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Station not found with ID: " + id);
        }
        stationRepository.deleteById(id);
    }

    @Override
    public FareRuleDto addFareRule(CreateFareRuleRequest fareRuleRequest) {
        // Step 1: Find the origin and destination stations from the database
        Station origin = stationRepository.findById(fareRuleRequest.getOriginStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Origin station with ID " + fareRuleRequest.getOriginStationId() + " not found"));
        Station destination = stationRepository.findById(fareRuleRequest.getDestinationStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination station with ID " + fareRuleRequest.getDestinationStationId() + " not found"));

        // Step 2: Create a new FareRule entity
        FareRule fareRule = new FareRule();
        fareRule.setOriginStation(origin);
        fareRule.setDestinationStation(destination);
        fareRule.setFare(fareRuleRequest.getFare());

        // Step 3: Save the new fare rule to the database
        FareRule savedFareRule = fareRuleRepository.save(fareRule);
        
        // Step 4: Map the saved entity to a DTO and return it
        return fareRuleMapper.entityToDto(savedFareRule);
    }
    
    @Override
    public StationDto updateStation(Integer stationId, CreateStationRequest updateRequest) {
        //checks existing station or throw an exception
        Station stationToUpdate = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + stationId));

        //update the stations properties
        stationToUpdate.setName(updateRequest.getName());
        stationToUpdate.setCode(updateRequest.getCode());

        //save the updated station to the database
        Station updatedStation = stationRepository.save(stationToUpdate);

        //map the updated entity back to a DTO and return it
        return stationMapper.entityToDto(updatedStation);
    }
}
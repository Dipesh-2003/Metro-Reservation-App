package com.aurionpro.app.service.implementation;

import com.aurionpro.app.dto.CreateFareSlabRequest;
import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.FareSlabDto;
import com.aurionpro.app.dto.StationDto;
import com.aurionpro.app.entity.FareSlab;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.FareSlabMapper;
import com.aurionpro.app.mapper.StationMapper;
import com.aurionpro.app.repository.FareSlabRepository;
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
    private final FareSlabRepository fareSlabRepository;
    private final StationMapper stationMapper;
    private final FareSlabMapper fareSlabMapper;

    @Override
    public StationDto addStation(CreateStationRequest createRequest) {
        Station newStation = stationMapper.dtoToEntity(createRequest);
        Station savedStation = stationRepository.save(newStation);
        return stationMapper.entityToDto(savedStation);
    }

    @Override
    public List<StationDto> addStations(List<CreateStationRequest> createRequests) {
        List<Station> stationsToSave = createRequests.stream()
                .map(stationMapper::dtoToEntity)
                .collect(Collectors.toList());

        List<Station> savedStations = stationRepository.saveAll(stationsToSave);

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
    public FareSlabDto addFareSlab(CreateFareSlabRequest fareSlabRequest) {
        FareSlab fareSlab = fareSlabMapper.dtoToEntity(fareSlabRequest);
        FareSlab savedFareSlab = fareSlabRepository.save(fareSlab);
        return fareSlabMapper.entityToDto(savedFareSlab);
    }

    @Override
    public StationDto updateStation(Integer stationId, CreateStationRequest updateRequest) {
        Station stationToUpdate = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + stationId));

        stationToUpdate.setName(updateRequest.getName());
        stationToUpdate.setCode(updateRequest.getCode());
        stationToUpdate.setStationOrder(updateRequest.getStationOrder());

        Station updatedStation = stationRepository.save(stationToUpdate);

        return stationMapper.entityToDto(updatedStation);
    }
}
package com.aurionpro.app.service.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<Station> stations = stationRepository.findAllByActiveTrue();
        return stationMapper.entityToDto(stations);
    }
    
    public List<StationDto> getAllStationsForAdmin() {
        List<Station> stations = stationRepository.findAll();
        return stationMapper.entityToDto(stations);
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

    @Override
    public void deactivateStation(Integer id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + id));
        station.setActive(false);
        stationRepository.save(station);
    }

    @Override
    public void activateStation(Integer id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + id));
        station.setActive(true);
        stationRepository.save(station);
    }

    @Override
    public List<FareSlabDto> getAllFareSlabs() {
        return fareSlabRepository.findAll().stream()
                .map(fareSlabMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FareSlabDto updateFareSlab(Integer slabId, CreateFareSlabRequest request) {
        FareSlab slab = fareSlabRepository.findById(slabId)
                .orElseThrow(() -> new ResourceNotFoundException("FareSlab not found with ID: " + slabId));

        slab.setMinStations(request.getMinStations());
        slab.setMaxStations(request.getMaxStations());
        slab.setFare(request.getFare());

        FareSlab updatedSlab = fareSlabRepository.save(slab);
        return fareSlabMapper.entityToDto(updatedSlab);
    }

    @Override
    public void deleteFareSlab(Integer slabId) {
        if (!fareSlabRepository.existsById(slabId)) {
            throw new ResourceNotFoundException("FareSlab not found with ID: " + slabId);
        }
        fareSlabRepository.deleteById(slabId);
    }
}

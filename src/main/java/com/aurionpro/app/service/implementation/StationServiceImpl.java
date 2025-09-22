package com.aurionpro.app.service.implementation;

import com.aurionpro.app.entity.Station;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.repository.StationRepository;
import com.aurionpro.app.service.StationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StationServiceImpl implements StationService {

    @Autowired
    private StationRepository stationRepository;

    @Override
    public Station addStation(Station station) {
        return stationRepository.save(station);
    }

    @Override
    public Station getStationById(Integer id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + id));
    }

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public void deleteStation(Integer id) {
        if (!stationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Station not found with ID: " + id);
        }
        stationRepository.deleteById(id);
    }
}
package com.aurionpro.app.service;

import com.aurionpro.app.entity.Station;
import java.util.List;

public interface StationService {
    Station addStation(Station station);
    Station getStationById(Integer id);
    List<Station> getAllStations();
    void deleteStation(Integer id);
}
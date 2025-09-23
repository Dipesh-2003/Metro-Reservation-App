package com.aurionpro.app.mapper;


import java.util.List;

import org.mapstruct.Mapper;

import com.aurionpro.app.dto.CreateStationRequest;
import com.aurionpro.app.dto.StationDto;
import com.aurionpro.app.entity.Station;

@Mapper(componentModel = "spring")
public interface StationMapper {
    StationDto entityToDto(Station station);
    List<StationDto> entityToDto(List<Station> stations);
    
    // New method to convert a request DTO to an entity
    Station dtoToEntity(CreateStationRequest createRequest);
}

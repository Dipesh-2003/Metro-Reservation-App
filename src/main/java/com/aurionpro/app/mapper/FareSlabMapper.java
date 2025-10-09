package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.CreateFareSlabRequest;
import com.aurionpro.app.dto.FareSlabDto;
import com.aurionpro.app.entity.FareSlab;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FareSlabMapper {
    FareSlabDto entityToDto(FareSlab fareSlab);
    FareSlab dtoToEntity(CreateFareSlabRequest createFareSlabRequest);
}
package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.FareRuleDto;
import com.aurionpro.app.entity.FareRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FareRuleMapper {

    @Mapping(source = "originStation.name", target = "originStationName")
    @Mapping(source = "destinationStation.name", target = "destinationStationName")
    FareRuleDto entityToDto(FareRule fareRule);
}
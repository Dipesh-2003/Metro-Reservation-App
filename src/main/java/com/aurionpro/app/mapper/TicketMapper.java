package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.TicketDto;
import com.aurionpro.app.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "originStation.name", target = "originStationName")
    @Mapping(source = "destinationStation.name", target = "destinationStationName")
    @Mapping(target = "qrCodeImage", ignore = true)
    TicketDto entityToDto(Ticket ticket);

    List<TicketDto> entityToDto(List<Ticket> tickets);
}

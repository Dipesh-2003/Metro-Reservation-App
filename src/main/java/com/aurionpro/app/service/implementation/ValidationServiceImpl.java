package com.aurionpro.app.service.implementation;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.common.TicketStatus;
import com.aurionpro.app.common.ValidationType;
import com.aurionpro.app.dto.QrPayloadDto;
import com.aurionpro.app.dto.ScanRequestDto;
import com.aurionpro.app.dto.ValidationHistoryDto;
import com.aurionpro.app.dto.ValidationResponseDto;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Validation;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.repository.StationRepository;
import com.aurionpro.app.repository.TicketRepository;
import com.aurionpro.app.repository.ValidationRepository;
import com.aurionpro.app.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final TicketRepository ticketRepository;
    private final ValidationRepository validationRepository;
    private final StationRepository stationRepository;
    private final ObjectMapper objectMapper; // Spring Boot provides this bean by default

    @Override
    @Transactional
    public ValidationResponseDto validateTicket(ScanRequestDto scanRequest, User staffMember) {
        try {
            // Step 1: Parse the QR Code JSON payload
            QrPayloadDto payload = objectMapper.readValue(scanRequest.getQrCodePayload(), QrPayloadDto.class);

            // Step 2: Find the ticket in the database
            Ticket ticket = ticketRepository.findByTicketNumber(payload.getTicketNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

            // Step 3: Apply validation rules
            if (ticket.getStatus() == TicketStatus.CANCELLED || ticket.getStatus() == TicketStatus.EXPIRED) {
                throw new InvalidOperationException("Ticket is invalid (status: " + ticket.getStatus() + ")");
            }
            if (ticket.getStatus() == TicketStatus.USED) {
                throw new InvalidOperationException("This ticket has already been used.");
            }
            if (ticket.getExpiryTime().isBefore(Instant.now())) {
                ticket.setStatus(TicketStatus.EXPIRED);
                ticketRepository.save(ticket);
                throw new InvalidOperationException("This ticket has expired.");
            }

            // Step 4: If all rules pass, the ticket is valid.
            // Update the ticket status to USED.
            ticket.setStatus(TicketStatus.USED);
            ticketRepository.save(ticket);
            
            // For now, we assume any scan is an ENTRY scan at a mock station.
            // A more complex implementation could determine Entry/Exit.
            Station mockStation = stationRepository.findById(1).orElse(null); // Assuming a station with ID 1 exists
            
            // Record this successful validation event
            Validation validation = new Validation();
            validation.setTicket(ticket);
            validation.setIsValid(true);
            validation.setStation(mockStation); 
            validation.setValidationTime(Instant.now());
            validation.setValidationType(ValidationType.ENTRY); 
            validationRepository.save(validation);

            // Return a successful response
            return new ValidationResponseDto(true, "Ticket Validated Successfully", ticket.getTicketNumber(), validation.getValidationTime());

        } catch (Exception e) {
            // If any exception occurs (JSON parsing, not found, invalid operation), record a failed validation
            Validation validation = new Validation();
            validation.setIsValid(false);
            validation.setValidationTime(Instant.now());
            // In a real scenario, you might log the failed payload or staff ID
            validationRepository.save(validation);
            
            // And return a failure response
            return new ValidationResponseDto(false, e.getMessage(), null, Instant.now());
        }
    }
    
    @Override
    public List<ValidationHistoryDto> getValidationHistory(Integer ticketId) {
        // Step 1: Fetch the ticket to ensure it exists
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket with ID " + ticketId + " not found."));

        // Step 2: Fetch the validation history for that ticket
        List<Validation> validations = validationRepository.findByTicketTicketIdOrderByValidationTimeDesc(ticket.getTicketId());

        // Step 3: Map the list of entities to a list of DTOs
        return validations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Helper method to map a single Validation entity to a DTO
    private ValidationHistoryDto mapToDto(Validation validation) {
        ValidationHistoryDto dto = new ValidationHistoryDto();
        dto.setValidationId(validation.getValidationId());
        dto.setTicketId(validation.getTicket().getTicketId());
        dto.setValidationTime(validation.getValidationTime());
        dto.setValidationType(validation.getValidationType());
        dto.setValid(validation.getIsValid());
        // Handle cases where station might be null (for failed scans)
        if (validation.getStation() != null) {
            dto.setStationName(validation.getStation().getName());
        } else {
            dto.setStationName("N/A");
        }
        return dto;
    }
}
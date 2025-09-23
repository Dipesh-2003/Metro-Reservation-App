package com.aurionpro.app.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.app.dto.BookingRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.dto.TicketDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.service.TicketService;
import com.aurionpro.app.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    private final UserService userService; 
    @GetMapping("/fare")
    public ResponseEntity<FareResponse> getFare(@RequestParam Integer originId, @RequestParam Integer destId) {
        return ResponseEntity.ok(ticketService.calculateFare(originId, destId));
    }

    @PostMapping("/book")
    public ResponseEntity<TicketDto> bookTicket(@RequestBody BookingRequest bookingRequest, Principal principal) {
        // Get the full User entity from the authenticated principal's email
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        return ResponseEntity.ok(ticketService.bookTicket(bookingRequest, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<TicketDto>> getTicketHistory(Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        return ResponseEntity.ok(ticketService.getTicketHistory(currentUser));
    }

    // Add other endpoints for getById and cancel
}
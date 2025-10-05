package com.aurionpro.app.controller;

import com.aurionpro.app.dto.BookingRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.dto.TicketDto;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.service.TicketService;
import com.aurionpro.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Tag(name = "3. Ticket Management", description = "APIs for booking tickets and viewing history")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    @GetMapping("/fare")
    @Operation(summary = "Calculate ticket fare", description = "Calculates the fare between two stations. Does not require authentication.")
    public ResponseEntity<FareResponse> getFare(@RequestParam Integer originId, @RequestParam Integer destId) {
        return ResponseEntity.ok(ticketService.calculateFare(originId, destId));
    }

    @PostMapping("/book")
    @Operation(summary = "Book a new ticket", description = "Books a ticket for the authenticated user. Requires USER role.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TicketDto> bookTicket(@RequestBody BookingRequest bookingRequest, Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        return ResponseEntity.ok(ticketService.bookTicket(bookingRequest, currentUser));
    }

    @GetMapping
    @Operation(summary = "Get user's ticket history", description = "Retrieves the ticket history for the authenticated user. Requires USER role.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<TicketDto>> getTicketHistory(Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        return ResponseEntity.ok(ticketService.getTicketHistory(currentUser));
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket details by ID", description = "Retrieves the details of a specific ticket by its ID.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable Integer ticketId, Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        TicketDto ticket = ticketService.getTicketByIdAndUser(ticketId, currentUser);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/{ticketId}/cancel")
    @Operation(summary = "Cancel a ticket", description = "Cancels a ticket if it is still valid and has not been used.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TicketDto> cancelTicket(@PathVariable Integer ticketId, Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        TicketDto cancelledTicket = ticketService.cancelTicket(ticketId, currentUser);
        return ResponseEntity.ok(cancelledTicket);
    }

    @GetMapping("/{ticketId}/download")
    @Operation(summary = "Download ticket QR code", description = "Downloads the QR code for a specific ticket as a PNG image.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<byte[]> downloadTicketQrCode(@PathVariable Integer ticketId, Principal principal) {
        User currentUser = userService.findUserEntityByEmail(principal.getName());
        TicketDto ticket = ticketService.getTicketByIdAndUser(ticketId, currentUser);

        // --- Use the new qrCodeImage field ---
        byte[] qrCodeImage = Base64.getDecoder().decode(ticket.getQrCodeImage());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "ticket_" + ticket.getTicketNumber() + ".png");

        return ResponseEntity.ok()
                .headers(headers)
                .body(qrCodeImage);
    }
}
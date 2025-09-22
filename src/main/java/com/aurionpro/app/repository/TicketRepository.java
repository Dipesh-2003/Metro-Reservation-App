package com.aurionpro.app.repository;

import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByUserOrderByBookingTimeDesc(User user);
}
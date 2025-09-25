package com.aurionpro.app.repository;

import com.aurionpro.app.dto.SalesReportDto;
import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByUserOrderByBookingTimeDesc(User user);
    
    Optional<Ticket> findByTicketIdAndUser(Integer ticketId, User user);
    
    @Query("SELECT new com.aurionpro.app.dto.SalesReportDto(" +
            ":startDate, :endDate, " +
            "COUNT(t), COALESCE(SUM(t.fare), 0.00)) " +
            "FROM Ticket t " +
            "WHERE t.issueDate BETWEEN :startDate AND :endDate " +
            "AND t.status <> com.aurionpro.app.common.TicketStatus.CANCELLED")
     SalesReportDto getSalesReport(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
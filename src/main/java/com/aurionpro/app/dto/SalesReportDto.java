package com.aurionpro.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalTicketsSold;
    private BigDecimal totalRevenue;
}
package com.aurionpro.app.entity;

import com.aurionpro.app.common.ValidationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
public class Validation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "validation_id")
	private Integer validationId;

	@ManyToOne
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;

	@ManyToOne
	@JoinColumn(name = "station_id", nullable = false)
	private Station station;

	@Column(name = "validation_time", nullable = false)
	private Instant validationTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "validation_type", nullable = false)
	private ValidationType validationType;

	@Column(name = "is_valid", nullable = false)
	private Boolean isValid;
}
package com.example.flight.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
	@NotBlank
    private String airlineName;
	@NotBlank
    private String fromPlace;
	@NotBlank
    private String toPlace;
	
	@NotNull
    private LocalDate scheduleDate;
	@NotNull
    private LocalTime departureTime;
	@NotNull
    private LocalTime arrivalTime;
	
	@Min(value = 0, message = "Price cannot be negative.")
    private Double price;
	@Min(value = 1, message = "Total seats must be at least 1.")
    private Integer totalSeats;
}

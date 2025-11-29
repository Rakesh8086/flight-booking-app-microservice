package com.example.flight.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {
    @NotBlank
    private String fromPlace;
    @NotBlank
    private String toPlace;
    @NotNull
    private LocalDate journeyDate;
}

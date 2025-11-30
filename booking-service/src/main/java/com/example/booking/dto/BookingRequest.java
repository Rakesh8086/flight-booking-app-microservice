package com.example.booking.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
	
	@NotNull(message = "Flight ID must be provided.")
    private Long flightId;
    @NotBlank
    private String userName;
    @NotBlank
    @Email(message = "Email must be a valid format.")
    private String userEmail;
    @NotNull(message = "Journey Date must be provided.")
    private LocalDate journeyDate;
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits.")
    private String mobileNumber; 

    @NotNull
    @Pattern(regexp = "Veg|NonVeg", message = "Meal option must be 'Veg' or 'NonVeg'.")
    private String mealOpted; 
    
    @Valid // This one checks validation on the list elements on PassengerDTO
    @NotEmpty(message = "Passenger details are required for booking.")
    private List<PassengerDTO> passengers;
}

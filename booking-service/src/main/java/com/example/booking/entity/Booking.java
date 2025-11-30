package com.example.booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings")
public class Booking {

    @Id
    private String pnr; 
    
    @NotBlank
    private String userName;
    @NotBlank
    @Email(message = "Email must be a valid format.")
    private String userEmail;
    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits.")
    private String mobileNumber;
    @NotNull
    private LocalDateTime bookingDate;
    @Min(value = 1, message = "Must book at least 1 seat.")
    private Integer numberOfSeats;
    @NotBlank
    private String mealOpted;
    private Double totalCost;

    // Storing the Flight ID as a foreign key for easy lookup
    @NotNull
    private Long flightId;
    
    // We store the journey date here for easy cancellation checks 
    @NotNull
    private LocalDate journeyDate; 
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Passenger> passengers;
    
}

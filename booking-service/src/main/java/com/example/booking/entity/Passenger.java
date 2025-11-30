package com.example.booking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "passengers")
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;    
    @NotBlank
    private String gender;  
    @Min(value = 0)
    private Integer age;    
    @NotBlank
    private String seatNumber;

    // Relationship to Booking Entity (Many Passengers belong to One Booking)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pnr_fk", nullable = false) 
    @JsonBackReference
    private Booking booking; // stores pnr as foreign key from passengers table

}

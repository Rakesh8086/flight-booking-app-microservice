package com.example.flight.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.flight.entity.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
	
	List<Flight> findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
            String fromPlace, 
            String toPlace, 
            LocalDate scheduleDate, 
            int availableSeats
    );
	
}
package com.example.flight.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.flight.dto.FlightDTO;
import com.example.flight.entity.Flight;

public interface FlightService {

    // used for adding flight
    Long addFlight(FlightDTO flight);
    
    // for searching flights
    List<Flight> searchFlights(String fromPlace, String toPlace, LocalDate scheduleDate);

    // get flight by its ID
    Optional<FlightDTO> getFlightById(Long flightId);
    
    // to update flight seats after booking tickets
    String updateFlightInventory(FlightDTO flightDto);
    
}
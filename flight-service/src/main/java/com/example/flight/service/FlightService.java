package com.example.flight.service;

import java.time.LocalDate;
import java.util.List;

import com.example.flight.dto.FlightDTO;
import com.example.flight.entity.Flight;

public interface FlightService {

    // used for adding flight
    Long addFlight(FlightDTO flight);
    
    // for searching flights
    List<Flight> searchFlights(String fromPlace, String toPlace, LocalDate scheduleDate);
}
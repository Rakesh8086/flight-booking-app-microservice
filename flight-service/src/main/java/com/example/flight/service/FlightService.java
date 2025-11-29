package com.example.flight.service;

import com.example.flight.dto.FlightDTO;

public interface FlightService {

    // used for adding flight
    Long addFlight(FlightDTO flight);
    
}
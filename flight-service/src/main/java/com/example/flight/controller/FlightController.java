package com.example.flight.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.flight.dto.FlightDTO;
import com.example.flight.dto.FlightSearchRequest;
import com.example.flight.entity.Flight;
import com.example.flight.service.FlightService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1.0/flight")
public class FlightController {
	
	@Autowired
    FlightService flightService;

    @PostMapping("/airline/inventory/add")
    public ResponseEntity<Long> addFlightInventory(@Valid @RequestBody FlightDTO flightDto) {
        
        Long savedFlight = flightService.addFlight(flightDto);
        
        return new ResponseEntity<>(savedFlight, HttpStatus.CREATED);
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        
        List<Flight> matchingFlights = flightService.searchFlights(
                request.getFromPlace(),
                request.getToPlace(),
                request.getJourneyDate()
        );

        if(matchingFlights.isEmpty()) {
            // no flights available
        	throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "No flights found matching the criteria from " + 
                    request.getFromPlace() + " to " + request.getToPlace() + 
                    " on " + request.getJourneyDate()
                );
        }

        return new ResponseEntity<>(matchingFlights, HttpStatus.OK);
    }
}
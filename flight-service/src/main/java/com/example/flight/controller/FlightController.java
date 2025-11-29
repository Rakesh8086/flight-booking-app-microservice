package com.example.flight.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flight.dto.FlightDTO;
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
}
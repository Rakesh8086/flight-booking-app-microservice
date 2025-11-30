package com.example.flight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<List<FlightDTO>> searchFlights(@Valid @RequestBody FlightSearchRequest request) {
        
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
        
        List<FlightDTO> flightDtos = matchingFlights.stream()
                .map(this::FlightEntityToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(flightDtos, HttpStatus.OK);
    }
    
    private FlightDTO FlightEntityToDto(Flight flight) {
    	FlightDTO flightDto = new FlightDTO();
    	
    	flightDto.setId(flight.getId());
    	flightDto.setAirlineName(flight.getAirlineName());
    	flightDto.setArrivalTime(flight.getArrivalTime());
    	flightDto.setDepartureTime(flight.getDepartureTime());
    	flightDto.setPrice(flight.getPrice());
    	flightDto.setScheduleDate(flight.getScheduleDate());
    	flightDto.setFromPlace(flight.getFromPlace());
    	flightDto.setToPlace(flight.getToPlace());
    	flightDto.setTotalSeats(flight.getTotalSeats());
    	flightDto.setAvailableSeats(flight.getAvailableSeats());
    	
    	return flightDto;
    }
    
    @GetMapping("/{flightId}")
    public ResponseEntity<FlightDTO> getFlightById(@PathVariable Long flightId) {
        
        FlightDTO flightDto = flightService.getFlightById(flightId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "Flight not found with ID: " + flightId
                ));
        
        return new ResponseEntity<>(flightDto, HttpStatus.OK);
    }
    
    
    @PatchMapping("/updateInventory")
    public ResponseEntity<String> updateFlightInventory(@Valid @RequestBody FlightDTO flightDto) {
        
        String response = flightService.updateFlightInventory(flightDto);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
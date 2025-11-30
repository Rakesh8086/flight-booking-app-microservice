package com.example.flight.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.flight.dto.FlightDTO;
import com.example.flight.entity.Flight;
import com.example.flight.exception.FlightNotFoundException;
import com.example.flight.repository.FlightRepository;
import com.example.flight.service.FlightService;


@Service
public class FlightServiceImpl implements FlightService {
	
	@Autowired
    FlightRepository flightRepository;

    @Override
    public Long addFlight(FlightDTO flightDto) {
    	if(flightDto.getFromPlace().equalsIgnoreCase(flightDto.getToPlace())) {
    		throw new IllegalArgumentException("Departure and arrival places cannot be the same.");
        }

        // Arrival time must be after Departure time
        LocalTime departure = flightDto.getDepartureTime();
        LocalTime arrival = flightDto.getArrivalTime();

        if(!arrival.isAfter(departure)) {
        	throw new IllegalArgumentException("Arrival time must be after the departure time.");
        }
        
    	Flight flight = FlightDtoToEntity(flightDto);
    	
        flightRepository.save(flight);
        
        return flight.getId();
    }
    
    @Override
    public List<Flight> searchFlights(String fromPlace, String toPlace, LocalDate scheduleDate) {
        return flightRepository.findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
                fromPlace, 
                toPlace, 
                scheduleDate, 
                0 // show flights with 1 or more available seats
        );
    }
    
    @Override
    public Optional<FlightDTO> getFlightById(Long flightId) {
    	return flightRepository.findById(flightId)
    	        .map(this::FlightEntityToDto);
    }
    
    @Override
    public String updateFlightInventory(FlightDTO flightDto) {
        if(flightDto.getId() == null) {
            throw new IllegalArgumentException("Flight ID is required for inventory update.");
        }
        Flight existingFlight = flightRepository.findById(flightDto.getId())
            .orElseThrow(() -> new FlightNotFoundException(
            		"Flight not found with ID: " + flightDto.getId()));
        existingFlight.setAvailableSeats(flightDto.getAvailableSeats());
        flightRepository.save(existingFlight);
        
        return "Inventory of Flight with Id " + existingFlight.getId() + " has been updated.";
    }
    
    private Flight FlightDtoToEntity(FlightDTO flightDto) {
    	Flight flight = new Flight();
    	
    	flight.setId(flightDto.getId()); 
        
        flight.setAirlineName(flightDto.getAirlineName());
        flight.setArrivalTime(flightDto.getArrivalTime());
        flight.setAvailableSeats(flightDto.getTotalSeats()); 
        flight.setDepartureTime(flightDto.getDepartureTime());
        flight.setPrice(flightDto.getPrice());
        flight.setScheduleDate(flightDto.getScheduleDate());
        flight.setFromPlace(flightDto.getFromPlace());
        flight.setToPlace(flightDto.getToPlace());
        flight.setTotalSeats(flightDto.getTotalSeats());
    	
    	return flight;
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
}
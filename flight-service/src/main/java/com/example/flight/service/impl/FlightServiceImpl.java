package com.example.flight.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.flight.dto.FlightDTO;
import com.example.flight.entity.Flight;
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
    public Optional<Flight> getFlightById(Long flightId) {
        return flightRepository.findById(flightId);
    }
    
    @Override
    public String updateFlightInventory(FlightDTO flightDto) {
    	Flight flight = FlightDtoToEntity(flightDto);
    	
        flightRepository.save(flight);
        
        return "Inventory of Flight with Id " + flight.getId() + "been updated.";
    }
    
    private Flight FlightDtoToEntity(FlightDTO flightDto) {
    	Flight flight = new Flight();
    	
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
}
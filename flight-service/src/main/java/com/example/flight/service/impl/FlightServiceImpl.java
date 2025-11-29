package com.example.flight.service.impl;

import java.time.LocalTime;

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
    	
        flightRepository.save(flight);
        
        return flight.getId();
    }

}
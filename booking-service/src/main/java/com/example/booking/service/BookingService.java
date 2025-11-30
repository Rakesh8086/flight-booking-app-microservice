package com.example.booking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.booking.dto.BookingRequest;
import com.example.booking.dto.FlightDTO;
import com.example.booking.dto.PassengerDTO;
import com.example.booking.entity.Booking;
import com.example.booking.entity.Passenger;
import com.example.booking.exception.BookingNotFoundException;
import com.example.booking.exception.FlightUnavailableException;
import com.example.booking.feign.BookingInterface;
import com.example.booking.repository.BookingRepository;
import com.google.common.base.Optional;

import feign.FeignException;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class BookingService {
	
	@Autowired
    BookingRepository bookingRepository;
	
	@Autowired
	BookingInterface bookingInterface;
	
	public BookingService(BookingRepository bookingRepository, 
			BookingInterface bookingInterface) {
        this.bookingRepository = bookingRepository;
        this.bookingInterface = bookingInterface;
    }


    // used for the ticket booking process. 
	// Booking object contains passenger details
	@Transactional
	public String bookTicket(Long flightId, BookingRequest bookingRequest) {
	    FlightDTO flightDto;
	    
	    try {
	        flightDto = bookingInterface.getFlightById(flightId); 
	    } catch (FeignException.NotFound e) {
	        throw new FlightUnavailableException("Flight with ID " + flightId + " not found.");
	    }
	    
	    int seatsToBook = bookingRequest.getPassengers().size();	    
	    if(seatsToBook <= 0) {
	        throw new FlightUnavailableException("Number of seats must be at least one.");
	    }

	    Integer currentAvailableSeats = flightDto.getAvailableSeats();
	    if(currentAvailableSeats < seatsToBook) {
	        throw new FlightUnavailableException(
	                "Insufficient seats available. Requested: " + seatsToBook + 
	                ", Available: " + currentAvailableSeats
	        );
	    }
	    
	    flightDto.setAvailableSeats(currentAvailableSeats - seatsToBook);
	    bookingInterface.updateFlightInventory(flightDto); 
	    
	    Booking booking = requestToEntity(bookingRequest, flightDto, flightId);
	    bookingRepository.save(booking);
	    
	    return booking.getPnr();
	}
	
	private Passenger mapPassengerDtoToEntity(PassengerDTO dto) {
        Passenger passenger = new Passenger();
        passenger.setName(dto.getName());
        passenger.setGender(dto.getGender());
        passenger.setAge(dto.getAge());
        passenger.setSeatNumber(dto.getSeatNumber());
        
        return passenger;
    }
    
	private Booking requestToEntity(BookingRequest bookingRequest, 
			FlightDTO flightDto, Long flightId) {
		
		Booking booking = new Booking();
		booking.setPnr(generateUniquePNR());
		booking.setUserEmail(bookingRequest.getUserEmail());
		booking.setUserName(bookingRequest.getUserName());
		booking.setNumberOfSeats(bookingRequest.getPassengers().size());
		booking.setBookingDate(LocalDateTime.now());
		booking.setJourneyDate(bookingRequest.getJourneyDate());
		booking.setFlightId(flightId);
		booking.setMealOpted(bookingRequest.getMealOpted());
		booking.setMobileNumber(bookingRequest.getMobileNumber());
		booking.setTotalCost(bookingRequest.getPassengers().size() * flightDto.getPrice());
		
		List<Passenger> passengers = bookingRequest.getPassengers().stream()
                .map(this::mapPassengerDtoToEntity)
                .collect(Collectors.toList());
		
		for(Passenger passenger : passengers) {
            passenger.setBooking(booking); 
        }
        
        booking.setPassengers(passengers); 
		
		return booking;
	}	
	
	private String generateUniquePNR() {
        return "CHUBBFLIGHT" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
	
	public Booking getTicketByPnr(String pnr) {
        Optional<Booking> bookingOptional = bookingRepository.findByPnr(pnr);
        if(!bookingOptional.isPresent()) {
        	throw new BookingNotFoundException(
        			"Number of seats must be at least one.");
        }
        
        return bookingOptional.get();
    }
    
}

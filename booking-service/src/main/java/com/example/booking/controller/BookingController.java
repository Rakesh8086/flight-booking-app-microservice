package com.example.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.dto.BookingRequest;
import com.example.booking.entity.Booking;
import com.example.booking.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1.0/booking")
public class BookingController {
	
	@Autowired
	BookingService bookingService;
	
	@PostMapping("/ticket/{flightId}")
    public ResponseEntity<String> bookTicket(
            @PathVariable Long flightId, 
            @Valid @RequestBody BookingRequest request) {
               
        String bookedTicketPnr = bookingService.bookTicket(flightId, request);
        
        return new ResponseEntity<>(bookedTicketPnr, HttpStatus.CREATED);
    }
	
	@GetMapping("/ticket/{pnr}")
    public ResponseEntity<Booking> getTicketByPnr(@PathVariable String pnr) {
        
        Booking booking = bookingService.getTicketByPnr(pnr);
        
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }
}

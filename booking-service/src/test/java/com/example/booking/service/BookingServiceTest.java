package com.example.booking.service;

import com.example.booking.dto.FlightDTO;
import com.example.booking.dto.PassengerDTO;
import com.example.booking.dto.BookingRequest;
import com.example.booking.entity.Booking;
import com.example.booking.entity.Passenger;
import com.example.booking.exception.BookingNotFoundException;
import com.example.booking.exception.FlightUnavailableException;
import com.example.booking.feign.BookingInterface;
import com.example.booking.repository.BookingRepository;

import feign.FeignException;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    // Mock the Feign Client interface
    @Mock
    private BookingInterface bookingInterface;

    @InjectMocks
    private BookingService bookingService;

    private Long flightId = 101L;
    private String pnr = "ABC123";
    private String userEmail = "test@example.com";
    private LocalDate journeyDate = LocalDate.now().plusDays(5); 
    private LocalTime departureTime = LocalTime.of(10, 0);

    private FlightDTO flightDto;
    private BookingRequest bookingRequest;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        flightDto = new FlightDTO(
            flightId, "Air India", "DEL", "BOM", journeyDate, 
            departureTime, LocalTime.of(12, 0), 5000.00, 150, 50 
        );

        bookingRequest = new BookingRequest();
        bookingRequest.setUserEmail(userEmail);
        
        List<Passenger> passengers = new ArrayList<>(); 
        List<PassengerDTO> passengerDTOs = new ArrayList<>();
        
        
        Passenger p1 = new Passenger();
        p1.setName("John Doe");
        p1.setGender("Male");
        p1.setAge(30);
        passengers.add(p1);

        PassengerDTO p1Dto = new PassengerDTO();
        p1Dto.setName(p1.getName());
        p1Dto.setGender(p1.getGender());
        p1Dto.setAge(p1.getAge());
        passengerDTOs.add(p1Dto);

        Passenger p2 = new Passenger();
        p2.setName("Jane Doe");
        p2.setGender("Female");
        p2.setAge(28);
        passengers.add(p2);

        PassengerDTO p2Dto = new PassengerDTO();
        p2Dto.setName(p2.getName());
        p2Dto.setGender(p2.getGender());
        p2Dto.setAge(p2.getAge());
        passengerDTOs.add(p2Dto);

        bookingRequest.setPassengers(passengerDTOs);

        testBooking = new Booking();
        testBooking.setPnr(pnr);
        testBooking.setFlightId(flightId);
        testBooking.setNumberOfSeats(2);
        testBooking.setJourneyDate(journeyDate);
        testBooking.setUserEmail(userEmail);
        testBooking.setTotalCost(10000.00);
        testBooking.setPassengers(passengers);
        // bi-directional mapping
        passengers.forEach(p -> p.setBooking(testBooking));
    }

    @Test
    void bookTicket_Success_ReturnsPnrAndUpdatesInventory() {
        // Feign client returns the Flight details
        when(bookingInterface.getFlightById(flightId)).thenReturn(flightDto);
                when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        
        when(bookingInterface.updateFlightInventory(any(FlightDTO.class)))
            .thenReturn("Inventory updated successfully.");

        ResponseEntity<String> response = bookingService.bookTicket(flightId, bookingRequest);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(bookingInterface, times(1)).getFlightById(flightId);
        verify(bookingInterface, times(1)).updateFlightInventory(argThat(dto -> 
            dto.getAvailableSeats() == 48 
        ));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void bookTicket_FlightNotFound_ReturnsServiceUnavailableFromFallback() {
        // This is the scenario where the Circuit Breaker is bypassed/not yet open
        when(bookingInterface.getFlightById(flightId)).thenThrow(FeignException.class);
        
        ResponseEntity<String> response = bookingService.bookTicket(flightId, bookingRequest);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getBody().contains("Ticket Booking is currently unavailable"));
        
        verify(bookingRepository, never()).save(any());
        verify(bookingInterface, never()).updateFlightInventory(any());
    }
    
    @Test
    void bookTicket_NotEnoughSeats_ThrowsFlightUnavailableException() {
        flightDto.setAvailableSeats(1);
        when(bookingInterface.getFlightById(flightId)).thenReturn(flightDto);

        assertThrows(FlightUnavailableException.class, () -> {
            bookingService.bookTicket(flightId, bookingRequest);
        });

        verify(bookingInterface, never()).updateFlightInventory(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getBookingHistoryByEmail_Success() {
        when(bookingRepository.findByUserEmailOrderByBookingDateDesc(anyString())).thenReturn(List.of(testBooking));

        List<Booking> history = bookingService.getBookingHistoryByEmail("test@example.com");

        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
    }

    @Test
    void getBookingHistoryByEmail_Failure_NotFound() {
        when(bookingRepository.findByUserEmailOrderByBookingDateDesc(anyString())).thenReturn(List.of());

        assertThrows(BookingNotFoundException.class, () -> 
            bookingService.getBookingHistoryByEmail("abc@example.com"));
    }
}

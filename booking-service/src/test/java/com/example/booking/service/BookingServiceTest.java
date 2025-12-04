package com.example.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.booking.dto.BookingRequest;
import com.example.booking.dto.FlightDTO;
import com.example.booking.dto.PassengerDTO;
import com.example.booking.entity.Booking;
import com.example.booking.exception.BookingNotFoundException;
import com.example.booking.exception.CancellationNotPossibleException;
import com.example.booking.exception.FlightUnavailableException;
import com.example.booking.feign.BookingInterface;
import com.example.booking.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingInterface bookingInterface;
    @InjectMocks
    private BookingService bookingService;
    private BookingRequest bookingRequest;
    private FlightDTO flightDto;

    @BeforeEach
    void setup() {
        PassengerDTO p1 = new PassengerDTO("XXX", "Male", 30, "1A");
        PassengerDTO p2 = new PassengerDTO("YYY", "Female", 28, "1B");

        bookingRequest = new BookingRequest(1L, "Test User", "test@example.com",
                LocalDate.now().plusDays(5), "1234567890", "Veg",
                List.of(p1, p2)
        );

        flightDto = new FlightDTO(1L, "Air India", "AAA", "BBB",
                LocalDate.now().plusDays(5), LocalTime.of(9, 0),
                LocalTime.of(11, 0), 5000.0, 100, 50
        );
    }

    @Test
    void bookTicket_successfulBooking_returnsPNR() {
        when(bookingInterface.getFlightById(1L)).thenReturn(flightDto);
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(i -> i.getArgument(0));
        ResponseEntity<String> response =
                bookingService.bookTicket(1L, bookingRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody()); 
        verify(bookingInterface).updateFlightInventory(any());
        verify(bookingRepository).save(any());
    }

    @Test
    void bookTicket_insufficientSeats_throwsException() {
        flightDto.setAvailableSeats(1); // but request needs 2 seats
        when(bookingInterface.getFlightById(1L)).thenReturn(flightDto);
        assertThrows(
                FlightUnavailableException.class,
                () -> bookingService.bookTicket(1L, bookingRequest)
        );
    }

    @Test
    void getTicketByPnr_notFound_throwsException() {
        when(bookingRepository.findByPnr("ABC123"))
                .thenReturn(Optional.empty());
        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getTicketByPnr("ABC123")
        );
    }

    @Test
    void getBookingHistory_empty_throwsException() {
        when(bookingRepository.findByUserEmailOrderByBookingDateDesc("x@y.com"))
                .thenReturn(List.of());
        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getBookingHistoryByEmail("x@y.com")
        );
    }

    @Test
    void cancelTicket_successfulCancellation() {
        Booking booking = new Booking();
        booking.setPnr("PNR123");
        booking.setFlightId(1L);
        booking.setJourneyDate(LocalDate.now().plusDays(3));
        booking.setNumberOfSeats(2);

        when(bookingRepository.findByPnr("PNR123"))
                .thenReturn(Optional.of(booking));
        when(bookingInterface.getFlightById(1L))
                .thenReturn(flightDto);
        bookingService.cancelTicket("PNR123");

        verify(bookingRepository).delete(booking);
        verify(bookingInterface).updateFlightInventory(any());
    }
    
    @Test
    void cancelTicket_afterDeadline_throwsException() {

        Booking booking = new Booking();
        booking.setPnr("PNR123");
        booking.setFlightId(1L);
        booking.setJourneyDate(LocalDate.now().plusDays(1)); 
        booking.setNumberOfSeats(1);

        when(bookingRepository.findByPnr("PNR123"))
                .thenReturn(Optional.of(booking));
        flightDto.setDepartureTime(LocalTime.now().plusHours(10));
        when(bookingInterface.getFlightById(1L))
                .thenReturn(flightDto);
        assertThrows(
                CancellationNotPossibleException.class,
                () -> bookingService.cancelTicket("PNR123")
        );
    }
}
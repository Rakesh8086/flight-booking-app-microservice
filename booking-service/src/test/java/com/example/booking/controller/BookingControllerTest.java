package com.example.booking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.example.booking.dto.BookingRequest;
import com.example.booking.dto.PassengerDTO;
import com.example.booking.entity.Booking;
import com.example.booking.exception.BookingNotFoundException;
import com.example.booking.exception.CancellationNotPossibleException;
import com.example.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    @Autowired
    private ObjectMapper objectMapper;
    private BookingRequest bookingRequest;
    private Booking booking;

    @BeforeEach
    void setup() {
        PassengerDTO p1 = new PassengerDTO("John", "Male", 30, "1A");
        PassengerDTO p2 = new PassengerDTO("Alice", "Female", 28, "1B");

        bookingRequest = new BookingRequest(1L, "Test User", "test@example.com",
                LocalDate.now().plusDays(5),"1234567890", "Veg",
                List.of(p1, p2)
        );

        booking = new Booking();
        booking.setPnr("PNR123");
        booking.setUserName("AAA");
        booking.setUserEmail("test@example.com");
        booking.setMobileNumber("1234567890");
        booking.setNumberOfSeats(2);
        booking.setJourneyDate(LocalDate.now().plusDays(5));
        booking.setFlightId(1L);
        booking.setMealOpted("Veg");
        booking.setTotalCost(10000.0);
    }
    
    @Test
    void bookTicket_serviceUnavailable_returns503() throws Exception {
        when(bookingService.bookTicket(anyLong(), any(BookingRequest.class)))
                .thenReturn(new ResponseEntity<>(
                        "Ticket Booking is currently unavailable due to Flight Service failure.",
                        HttpStatus.SERVICE_UNAVAILABLE));

        mockMvc.perform(post("/api/v1.0/booking/ticket/{flightId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(
                "Ticket Booking is currently unavailable due to Flight Service failure."));
    }

    @Test
    void bookTicket_success_returnsCreated() throws Exception {
        when(bookingService.bookTicket(anyLong(), any(BookingRequest.class)))
                .thenReturn(new ResponseEntity<>("PNR123", HttpStatus.CREATED));
        
        mockMvc.perform(post("/api/v1.0/booking/ticket/{flightId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("PNR123"));
    }
    
    @Test
    void getTicketByPnr_found_returnsBooking() throws Exception {
        when(bookingService.getTicketByPnr("PNR123")).thenReturn(booking);

        mockMvc.perform(get("/api/v1.0/booking/ticket/{pnr}", "PNR123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pnr").value("PNR123"))
                .andExpect(jsonPath("$.userName").value("AAA"));
    }

    @Test
    void getTicketByPnr_notFound_returns404() throws Exception {
        when(bookingService.getTicketByPnr("PNR999"))
                .thenThrow(new BookingNotFoundException("Ticket not found"));

        mockMvc.perform(get("/api/v1.0/booking/ticket/{pnr}", "PNR999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Ticket not found"));
    }

    @Test
    void cancelTicket_success_returnsOk() throws Exception {
        doNothing().when(bookingService).cancelTicket("PNR123");

        mockMvc.perform(delete("/api/v1.0/booking/booking/cancel/{pnr}", "PNR123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Ticket with PNR PNR123 cancelled successfully."));
    }

    @Test
    void cancelTicket_cannotCancel_returns400() throws Exception {
        doThrow(new CancellationNotPossibleException(
                "Cannot cancel")).when(bookingService).cancelTicket("PNR123");

        mockMvc.perform(delete("/api/v1.0/booking/booking/cancel/{pnr}", "PNR123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot cancel"));
    }
}

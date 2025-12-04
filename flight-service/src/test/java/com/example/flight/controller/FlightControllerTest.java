package com.example.flight.controller;

import com.example.flight.dto.FlightDTO;
import com.example.flight.dto.FlightSearchRequest;
import com.example.flight.entity.Flight;
import com.example.flight.service.FlightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FlightService flightService;
    @Autowired
    private ObjectMapper objectMapper;

    private FlightDTO sampleFlight() {
        FlightDTO flightDto = new FlightDTO();
        flightDto.setId(1L);
        flightDto.setAirlineName("Indigo");
        flightDto.setFromPlace("AAA");
        flightDto.setToPlace("BBB");
        flightDto.setScheduleDate(LocalDate.of(2025, 12, 25));
        flightDto.setDepartureTime(LocalTime.of(10, 0));
        flightDto.setArrivalTime(LocalTime.of(12, 0));
        flightDto.setPrice(100.0);
        flightDto.setTotalSeats(100);
        flightDto.setAvailableSeats(50);
        return flightDto;
    }
    @Test
    void addFlightInventory_success_returnsCreatedId() throws Exception {
        when(flightService.addFlight(any(FlightDTO.class))).thenReturn(42L);
        FlightDTO dto = sampleFlight();
        mockMvc.perform(
                post("/api/v1.0/flight/airline/inventory/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
        .andExpect(status().isCreated())
        .andExpect(content().string("42"));
    }

    @Test
    void searchFlights_success_returnsList() throws Exception {
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setAirlineName("Indigo");
        flight.setFromPlace("AAA");
        flight.setToPlace("BBB");
        flight.setScheduleDate(LocalDate.of(2025, 12, 25));
        flight.setDepartureTime(LocalTime.of(10, 0));
        flight.setArrivalTime(LocalTime.of(12, 0));
        flight.setTotalSeats(100);
        flight.setAvailableSeats(60);
        flight.setPrice(100.0);

        when(flightService.searchFlights("AAA", "BBB", LocalDate.of(2025, 12, 25)))
                .thenReturn(List.of(flight));
        FlightSearchRequest request = new FlightSearchRequest("AAA", "BBB", LocalDate.of(2025, 12, 25));
        mockMvc.perform(
                post("/api/v1.0/flight/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].airlineName").value("Indigo"))
        .andExpect(jsonPath("$[0].fromPlace").value("AAA"))
        .andExpect(jsonPath("$[0].toPlace").value("BBB"));
    }

    @Test
    void searchFlights_noResults_returnsNotFound() throws Exception {
        when(flightService.searchFlights(anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        FlightSearchRequest request = new FlightSearchRequest("AAA", "BBB", LocalDate.of(2025, 12, 25));
        mockMvc.perform(
                post("/api/v1.0/flight/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void getFlightById_success_returnsDto() throws Exception {
        FlightDTO dto = sampleFlight();
        when(flightService.getFlightById(1L)).thenReturn(Optional.of(dto));
        mockMvc.perform(
                get("/api/v1.0/flight/1")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.airlineName").value("Indigo"));
    }

    @Test
    void getFlightById_notFound_returns404() throws Exception {
        when(flightService.getFlightById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(
                get("/api/v1.0/flight/1")
        )
        .andExpect(status().isNotFound());
    }
}
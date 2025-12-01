package com.example.flight.service;

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

import com.example.flight.dto.FlightDTO;
import com.example.flight.entity.Flight;
import com.example.flight.repository.FlightRepository;
import com.example.flight.service.impl.FlightServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight testFlightEntity;
    private FlightDTO testFlightDTO;

    private Flight DTOToEntity(FlightDTO dto) {
        Flight entity = new Flight();
        entity.setId(dto.getId());
        entity.setAirlineName(dto.getAirlineName());
        entity.setFromPlace(dto.getFromPlace());
        entity.setToPlace(dto.getToPlace());
        entity.setScheduleDate(dto.getScheduleDate());
        entity.setDepartureTime(dto.getDepartureTime());
        entity.setArrivalTime(dto.getArrivalTime());
        entity.setPrice(dto.getPrice());
        entity.setTotalSeats(dto.getTotalSeats());
        entity.setAvailableSeats(dto.getAvailableSeats());
        return entity;
    }

    @BeforeEach
    void setUp() {
        testFlightEntity = new Flight(101L, "Air India", "DEL", "BOM",
                LocalDate.of(2025, 12, 25), LocalTime.of(8, 0),
                LocalTime.of(10, 0), 5000.00, 150, 150);

        testFlightDTO = new FlightDTO(101L, "Air India", "DEL", "BOM",
                LocalDate.of(2025, 12, 25), LocalTime.of(8, 0),
                LocalTime.of(10, 0), 5000.00, 150, 150);
    }

    @Test
    void addFlight_Failure_SamePlaceThrowsException() {
        FlightDTO invalidDto = new FlightDTO(null, "Air India", "DEL", "DEL",
                LocalDate.of(2025, 12, 25), LocalTime.of(8, 0),
                LocalTime.of(10, 0), 5000.00, 150, 150);

        assertThrows(IllegalArgumentException.class, () -> {
            flightService.addFlight(invalidDto);
        });

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void searchFlights_Success_ReturnsMatchingFlights() {
        String from = "DEL";
        String to = "BOM";
        LocalDate date = LocalDate.of(2025, 12, 25);

        when(flightRepository.findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
                from, to, date, 0)).thenReturn(List.of(testFlightEntity));

        List<Flight> results = flightService.searchFlights(from, to, date);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testFlightEntity.getId(), results.get(0).getId());

        verify(flightRepository, times(1)).findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
                from, to, date, 0);
    }

    @Test
    void getFlightById_Success_ReturnsFlightDTO() {
        Long flightId = 101L;

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(testFlightEntity));

        Optional<FlightDTO> foundDtoOptional = flightService.getFlightById(flightId);
        assertTrue(foundDtoOptional.isPresent());
        assertEquals(flightId, foundDtoOptional.get().getId());
        assertEquals(testFlightDTO.getAirlineName(), foundDtoOptional.get().getAirlineName());

        verify(flightRepository, times(1)).findById(flightId);
    }

    @Test
    void getFlightById_NotFound_ReturnsEmptyOptional() {
        Long flightId = 102L;

        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());
        Optional<FlightDTO> foundDtoOptional = flightService.getFlightById(flightId);
        assertFalse(foundDtoOptional.isPresent());
    }

    @Test
    void updateFlightInventory_Success() {
        Long flightId = 101L;
        int newAvailableSeats = 50;

        FlightDTO updateDto = new FlightDTO();
        updateDto.setId(flightId);
        updateDto.setAvailableSeats(newAvailableSeats);

        Flight retrievedEntity = DTOToEntity(testFlightDTO);
        when(flightRepository.findById(flightId)).thenReturn(Optional.of(retrievedEntity));

        Flight savedEntity = DTOToEntity(testFlightDTO);
        savedEntity.setAvailableSeats(newAvailableSeats);
        when(flightRepository.save(any(Flight.class))).thenReturn(savedEntity);

        String responseMessage = flightService.updateFlightInventory(updateDto);
        assertTrue(responseMessage.contains("updated."));

        verify(flightRepository, times(1)).findById(flightId);
        verify(flightRepository, times(1)).save(argThat(flight -> flight.getId().equals(flightId) &&
                flight.getAvailableSeats().equals(newAvailableSeats)));
    }

    @Test
    void updateFlightInventory_NotFound_ThrowsException() {
        FlightDTO nonExistentDto = new FlightDTO();
        nonExistentDto.setId(999L);

        when(flightRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            flightService.updateFlightInventory(nonExistentDto);
        });

        verify(flightRepository, never()).save(any(Flight.class));
    }
}
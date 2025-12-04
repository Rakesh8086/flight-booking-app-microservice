package com.example.flight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.flight.dto.FlightDTO;
import com.example.flight.entity.Flight;
import com.example.flight.exception.FlightNotFoundException;
import com.example.flight.repository.FlightRepository;
import com.example.flight.service.impl.FlightServiceImpl;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;
    @InjectMocks
    private FlightServiceImpl flightService;
    private FlightDTO flightDto;

    @BeforeEach
    void setUp() {
        flightDto = new FlightDTO();
        flightDto.setAirlineName("TestAir");
        flightDto.setFromPlace("AAA");
        flightDto.setToPlace("BBB");
        flightDto.setScheduleDate(LocalDate.of(2025, 12, 25));
        flightDto.setDepartureTime(LocalTime.of(10, 0));
        flightDto.setArrivalTime(LocalTime.of(12, 0));
        flightDto.setPrice(150.0);
        flightDto.setTotalSeats(100);
    }

    @Test
    void addFlight_success_returnsGeneratedId() {
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> {
            Flight saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        Long id = flightService.addFlight(flightDto);

        assertNotNull(id);
        assertEquals(42L, id);

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository, times(1)).save(captor.capture());
        Flight flight = captor.getValue();

        assertEquals(flightDto.getAirlineName(), flight.getAirlineName());
        assertEquals(flightDto.getFromPlace(), flight.getFromPlace());
        assertEquals(flightDto.getToPlace(), flight.getToPlace());
        assertEquals(flightDto.getDepartureTime(), flight.getDepartureTime());
        assertEquals(flightDto.getArrivalTime(), flight.getArrivalTime());
        assertEquals(flightDto.getTotalSeats(), flight.getTotalSeats());
        assertEquals(flightDto.getTotalSeats(), flight.getAvailableSeats());
    }

    @Test
    void addFlight_sameFromAndTo_throwsIllegalArgumentException() {
        FlightDTO dto = new FlightDTO(flightDto.getId(), flightDto.getAirlineName(),
                "SAME", "same", flightDto.getScheduleDate(),
                flightDto.getDepartureTime(), flightDto.getArrivalTime(),
                flightDto.getPrice(), flightDto.getTotalSeats(), flightDto.getAvailableSeats());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> flightService.addFlight(dto));
        assertEquals("Departure and arrival places cannot be the same.", ex.getMessage());
        verifyNoInteractions(flightRepository);
    }

    @Test
    void addFlight_arrivalNotAfterDeparture_throwsIllegalArgumentException() {
        FlightDTO dto = new FlightDTO();
        dto.setAirlineName("XYX");
        dto.setFromPlace("ABC");
        dto.setToPlace("BCD");
        dto.setScheduleDate(LocalDate.now().plusDays(1));
        dto.setDepartureTime(LocalTime.of(10, 0));
        dto.setArrivalTime(LocalTime.of(10, 0)); 
        dto.setPrice(10.0);
        dto.setTotalSeats(10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> flightService.addFlight(dto));
        assertEquals("Arrival time must be after the departure time.", ex.getMessage());
        verifyNoInteractions(flightRepository);
    }

    @Test
    void searchFlights_returnsMatchingFlights() {
        Flight flight1 = new Flight();
        flight1.setId(1L);
        flight1.setAirlineName("A1");
        flight1.setFromPlace("AAA");
        flight1.setToPlace("BBB");
        flight1.setScheduleDate(flightDto.getScheduleDate());
        flight1.setAvailableSeats(5);

        when(flightRepository.findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan(
                "AAA", "BBB", flightDto.getScheduleDate(), 0))
            .thenReturn(Arrays.asList(flight1));

        List<Flight> results = flightService.searchFlights("AAA", "BBB", flightDto.getScheduleDate());

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(flight1.getId(), results.get(0).getId());
        verify(flightRepository, times(1))
            .findByFromPlaceAndToPlaceAndScheduleDateAndAvailableSeatsGreaterThan("AAA", "BBB", flightDto.getScheduleDate(), 0);
    }

    @Test
    void getFlightById_whenPresent_returnsDtoOptional() {
        Flight flight = new Flight();
        flight.setId(100L);
        flight.setAirlineName("Air100");
        flight.setFromPlace("XXX");
        flight.setToPlace("YYY");
        flight.setScheduleDate(LocalDate.of(2025, 12, 31));
        flight.setDepartureTime(LocalTime.of(8, 0));
        flight.setArrivalTime(LocalTime.of(10, 0));
        flight.setPrice(200.0);
        flight.setTotalSeats(50);
        flight.setAvailableSeats(20);

        when(flightRepository.findById(100L)).thenReturn(Optional.of(flight));

        Optional<FlightDTO> dtoOpt = flightService.getFlightById(100L);

        assertTrue(dtoOpt.isPresent());
        FlightDTO dto = dtoOpt.get();
        assertEquals(flight.getId(), dto.getId());
        assertEquals(flight.getAirlineName(), dto.getAirlineName());
        assertEquals(flight.getAvailableSeats(), dto.getAvailableSeats());
        verify(flightRepository, times(1)).findById(100L);
    }

    @Test
    void getFlightById_whenAbsent_returnsEmptyOptional() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<FlightDTO> dtoOpt = flightService.getFlightById(999L);

        assertFalse(dtoOpt.isPresent());
        verify(flightRepository, times(1)).findById(999L);
    }

    @Test
    void updateFlightInventory_success_updatesAvailableSeatsAndReturnsMessage() {
        Flight existing = new Flight();
        existing.setId(5L);
        existing.setAvailableSeats(10);
        existing.setTotalSeats(10);

        FlightDTO updateDto = new FlightDTO();
        updateDto.setId(5L);
        updateDto.setAvailableSeats(4);

        when(flightRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = flightService.updateFlightInventory(updateDto);
        assertNotNull(result);
        assertTrue(result.contains("Inventory of Flight with Id 5 has been updated."));
        assertEquals(4, existing.getAvailableSeats());
        verify(flightRepository, times(1)).findById(5L);
        verify(flightRepository, times(1)).save(existing);
    }

    @Test
    void updateFlightInventory_nonExistent_throwsFlightNotFoundException() {
        FlightDTO dto = new FlightDTO();
        dto.setId(777L);
        dto.setAvailableSeats(2);

        when(flightRepository.findById(777L)).thenReturn(Optional.empty());
        assertThrows(FlightNotFoundException.class, () -> flightService.updateFlightInventory(dto));

        verify(flightRepository, times(1)).findById(777L);
        verify(flightRepository, never()).save(any(Flight.class));
    }
}
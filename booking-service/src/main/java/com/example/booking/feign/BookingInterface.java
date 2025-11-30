package com.example.booking.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.booking.dto.FlightDTO;

@FeignClient("FLIGHT-SERVICE")
public interface BookingInterface {
	
	@GetMapping("/api/v1.0/flight/{flightId}") 
    public FlightDTO getFlightById(@PathVariable("flightId") Long flightId); 
    
    @RequestMapping(
            method = RequestMethod.PUT, 
            value = "/api/v1.0/flight/updateInventory" 
        )
    public String updateFlightInventory(FlightDTO flightDto);
}

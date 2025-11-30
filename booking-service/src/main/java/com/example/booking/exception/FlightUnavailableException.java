package com.example.booking.exception;

public class FlightUnavailableException extends RuntimeException {
	public FlightUnavailableException(String message) {
	    super(message);
	}
}

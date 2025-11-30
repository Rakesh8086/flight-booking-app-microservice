package com.example.booking.exception;

public class CancellationNotPossibleException extends RuntimeException{
	public CancellationNotPossibleException(String message) {
	    super(message);
	}
}
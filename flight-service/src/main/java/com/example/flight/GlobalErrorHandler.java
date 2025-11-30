package com.example.flight;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import com.example.flight.exception.FlightNotFoundException;

import org.springframework.web.bind.MethodArgumentNotValidException;

@ControllerAdvice
public class GlobalErrorHandler {
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName;
            if (error instanceof FieldError fieldError) {
                fieldName = fieldError.getField();
            } 
            else {
                fieldName = error.getObjectName(); 
            }
            String message = error.getDefaultMessage();
            errorMap.put(fieldName, message);
        });
        
        // 400 Bad request
        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST); 
    }
	
	@ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleServiceValidationExceptions(IllegalArgumentException ex) {
        // 400 Bad Request
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
	
	@ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        // Return a reason for the exception
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }
	
	@ExceptionHandler(FlightNotFoundException.class)
    public ResponseEntity<String> handleFlightNotFoundException(FlightNotFoundException ex) {
        
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

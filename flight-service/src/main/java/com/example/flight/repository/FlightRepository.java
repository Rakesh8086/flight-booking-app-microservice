package com.example.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.flight.entity.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

}
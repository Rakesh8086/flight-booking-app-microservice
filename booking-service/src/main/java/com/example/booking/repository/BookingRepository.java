package com.example.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
	
}

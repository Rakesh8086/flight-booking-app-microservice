package com.example.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.booking.entity.Booking;
import com.google.common.base.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
	Optional<Booking> findByPnr(String pnr);
	
	List<Booking> findByUserEmailOrderByBookingDateDesc(String userEmail);
}

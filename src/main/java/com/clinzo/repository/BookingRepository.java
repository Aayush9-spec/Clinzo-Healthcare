package com.clinzo.repository;

import com.clinzo.entity.Booking;
import com.clinzo.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findBySlotIdAndBookingStatus(UUID slotId, BookingStatus bookingStatus);
}

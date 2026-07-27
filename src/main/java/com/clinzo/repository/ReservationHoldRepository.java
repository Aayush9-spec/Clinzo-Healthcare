package com.clinzo.repository;

import com.clinzo.entity.ReservationHold;
import com.clinzo.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationHoldRepository extends JpaRepository<ReservationHold, UUID> {
    Optional<ReservationHold> findBySlotIdAndStatus(UUID slotId, ReservationStatus status);

    List<ReservationHold> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant cutoff);
}

package com.clinzo.repository;

import com.clinzo.entity.Slot;
import com.clinzo.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SlotRepository extends JpaRepository<Slot, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") UUID id);

    List<Slot> findByDoctorIdAndStartTimeBetweenAndStatusIn(UUID doctorId, Instant from, Instant to, List<SlotStatus> statuses);

    List<Slot> findByAvailabilityIdAndStartTimeAfterAndStatusIn(UUID availabilityId, Instant after, List<SlotStatus> statuses);
}

package com.clinzo.repository;

import com.clinzo.entity.Availability;
import com.clinzo.entity.Doctor;
import com.clinzo.entity.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    List<Availability> findByDoctorAndStatus(Doctor doctor, AvailabilityStatus status);

    @Query("SELECT a FROM Availability a WHERE a.doctor = :doctor AND a.status = 'ACTIVE' "
            + "AND ((a.date = :date) OR (a.recurringWeekday = :weekday))")
    List<Availability> findActiveByDoctorDateOrWeekday(@Param("doctor") Doctor doctor,
                                                       @Param("date") LocalDate date,
                                                       @Param("weekday") DayOfWeek weekday);

    @Query("SELECT a FROM Availability a WHERE a.doctor = :doctor AND a.status = 'ACTIVE' "
            + "AND ((a.date = :date) OR (a.recurringWeekday = :weekday)) "
            + "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Availability> findOverlapping(@Param("doctor") Doctor doctor,
                                       @Param("date") LocalDate date,
                                       @Param("weekday") DayOfWeek weekday,
                                       @Param("startTime") LocalTime startTime,
                                       @Param("endTime") LocalTime endTime);
}

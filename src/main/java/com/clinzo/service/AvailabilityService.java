package com.clinzo.service;

import com.clinzo.dto.AvailabilityRequest;
import com.clinzo.dto.AvailabilityResponse;
import com.clinzo.dto.SlotResponse;
import com.clinzo.entity.*;
import com.clinzo.exception.BadRequestException;
import com.clinzo.exception.ConflictException;
import com.clinzo.exception.ResourceNotFoundException;
import com.clinzo.mapper.AvailabilityMapper;
import com.clinzo.mapper.SlotMapper;
import com.clinzo.repository.AvailabilityRepository;
import com.clinzo.repository.DoctorRepository;
import com.clinzo.repository.SlotRepository;
import com.clinzo.util.DateTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {
    private final AvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final SlotRepository slotRepository;
    private final AvailabilityMapper availabilityMapper;
    private final SlotMapper slotMapper;
    private final AuditLogService auditLogService;

    public AvailabilityService(AvailabilityRepository availabilityRepository,
                               DoctorRepository doctorRepository,
                               SlotRepository slotRepository,
                               AvailabilityMapper availabilityMapper,
                               SlotMapper slotMapper,
                               AuditLogService auditLogService) {
        this.availabilityRepository = availabilityRepository;
        this.doctorRepository = doctorRepository;
        this.slotRepository = slotRepository;
        this.availabilityMapper = availabilityMapper;
        this.slotMapper = slotMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AvailabilityResponse createAvailability(AvailabilityRequest request) {
        validateAvailabilityRequest(request);
        Doctor doctor = findDoctor(request.doctorId());
        ensureNoOverlap(request, doctor, null);

        Availability availability = Availability.builder()
                .doctor(doctor)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .recurringWeekday(request.recurringWeekday())
                .status(AvailabilityStatus.ACTIVE)
                .build();
        Availability saved = availabilityRepository.save(availability);
        auditLogService.record("CREATE_AVAILABILITY", "Availability", saved.getId().toString(), "doctorId=" + saved.getDoctor().getId());
        return availabilityMapper.toResponse(saved);
    }

    @Transactional
    public AvailabilityResponse updateAvailability(UUID availabilityId, AvailabilityRequest request) {
        validateAvailabilityRequest(request);
        Availability availability = findAvailability(availabilityId);
        if (!availability.getDoctor().getId().equals(request.doctorId())) {
            throw new BadRequestException("Doctor id does not match availability record");
        }
        if (availability.getStatus() != AvailabilityStatus.ACTIVE) {
            throw new BadRequestException("Only active availability can be updated");
        }
        ensureNoOverlap(request, availability.getDoctor(), availabilityId);
        blockFutureFreeSlots(availability);

        availability.setDate(request.date());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());
        availability.setRecurringWeekday(request.recurringWeekday());

        auditLogService.record("UPDATE_AVAILABILITY", "Availability", availability.getId().toString(), "doctorId=" + availability.getDoctor().getId());
        return availabilityMapper.toResponse(availabilityRepository.save(availability));
    }

    @Transactional
    public void deleteAvailability(UUID availabilityId) {
        Availability availability = findAvailability(availabilityId);
        if (availability.getStatus() == AvailabilityStatus.INACTIVE) {
            return;
        }
        availability.setStatus(AvailabilityStatus.INACTIVE);
        blockFutureFreeSlots(availability);
        auditLogService.record("DELETE_AVAILABILITY", "Availability", availability.getId().toString(), "doctorId=" + availability.getDoctor().getId());
        availabilityRepository.save(availability);
    }

    @Transactional
    public List<SlotResponse> generateSlots(UUID availabilityId) {
        Availability availability = findAvailability(availabilityId);
        if (availability.getStatus() != AvailabilityStatus.ACTIVE) {
            throw new BadRequestException("Availability is not active");
        }
        blockFutureFreeSlots(availability);

        Doctor doctor = availability.getDoctor();
        Instant windowStart = DateTimeUtil.toUtcInstant(availability.getDate(), availability.getStartTime(), doctor.getTimezone());
        Instant windowEnd = DateTimeUtil.toUtcInstant(availability.getDate(), availability.getEndTime(), doctor.getTimezone());
        if (!windowStart.isBefore(windowEnd)) {
            throw new BadRequestException("Availability start time must be before end time");
        }

        Duration slotDuration = Duration.ofMinutes(doctor.getSlotDuration());
        Duration bufferTime = Duration.ofMinutes(doctor.getBufferTime());
        List<Slot> generatedSlots = new ArrayList<>();
        Instant nextStart = windowStart;
        while (nextStart.plus(slotDuration).compareTo(windowEnd) <= 0) {
            generatedSlots.add(Slot.builder()
                    .doctor(doctor)
                    .availability(availability)
                    .startTime(nextStart)
                    .endTime(nextStart.plus(slotDuration))
                    .status(SlotStatus.AVAILABLE)
                    .build());
            nextStart = nextStart.plus(slotDuration).plus(bufferTime);
        }

        List<Slot> saved = slotRepository.saveAll(generatedSlots);
        auditLogService.record("GENERATE_SLOTS", "Availability", availabilityId.toString(), "generatedCount=" + saved.size());
        return saved.stream().map(slotMapper::toResponse).collect(Collectors.toList());
    }

    private void validateAvailabilityRequest(AvailabilityRequest request) {
        if (request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime())) {
            throw new BadRequestException("Availability start time must be before end time");
        }
        if (!DateTimeUtil.isFuture(request.date())) {
            throw new BadRequestException("Availability date must be today or in the future");
        }
        if (request.recurringWeekday() != null && !request.recurringWeekday().equals(request.date().getDayOfWeek())) {
            throw new BadRequestException("Recurring weekday must match the availability date");
        }
    }

    private Doctor findDoctor(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }

    private Availability findAvailability(UUID availabilityId) {
        return availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));
    }

    private void ensureNoOverlap(AvailabilityRequest request, Doctor doctor, UUID currentAvailabilityId) {
        List<Availability> existing = availabilityRepository.findOverlapping(doctor, request.date(), request.recurringWeekday(), request.startTime(), request.endTime());
        if (currentAvailabilityId != null) {
            existing.removeIf(item -> item.getId().equals(currentAvailabilityId));
        }
        if (!existing.isEmpty()) {
            throw new ConflictException("Availability window overlaps with an existing schedule");
        }
    }

    private void blockFutureFreeSlots(Availability availability) {
        List<Slot> futureFreeSlots = slotRepository.findByAvailabilityIdAndStartTimeAfterAndStatusIn(
                availability.getId(), Instant.now(), List.of(SlotStatus.AVAILABLE, SlotStatus.HELD));
        futureFreeSlots.forEach(slot -> slot.setStatus(SlotStatus.BLOCKED));
        if (!futureFreeSlots.isEmpty()) {
            slotRepository.saveAll(futureFreeSlots);
        }
    }
}

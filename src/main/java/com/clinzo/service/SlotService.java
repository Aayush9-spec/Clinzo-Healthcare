package com.clinzo.service;

import com.clinzo.dto.SlotResponse;
import com.clinzo.entity.Doctor;
import com.clinzo.entity.SlotStatus;
import com.clinzo.exception.ResourceNotFoundException;
import com.clinzo.mapper.SlotMapper;
import com.clinzo.repository.DoctorRepository;
import com.clinzo.repository.SlotRepository;
import com.clinzo.util.DateTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SlotService {
    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;
    private final SlotMapper slotMapper;

    public SlotService(SlotRepository slotRepository, DoctorRepository doctorRepository, SlotMapper slotMapper) {
        this.slotRepository = slotRepository;
        this.doctorRepository = doctorRepository;
        this.slotMapper = slotMapper;
    }

    public List<SlotResponse> getAvailableSlots(UUID doctorId, LocalDate localDate) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        ZoneId zoneId = ZoneId.of(doctor.getTimezone());
        Instant startOfDay = localDate.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = localDate.plusDays(1).atStartOfDay(zoneId).toInstant();
        return slotRepository.findByDoctorIdAndStartTimeBetweenAndStatusIn(doctorId, startOfDay, endOfDay, List.of(SlotStatus.AVAILABLE))
                .stream().map(slotMapper::toResponse).collect(Collectors.toList());
    }
}

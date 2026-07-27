package com.clinzo.service;

import com.clinzo.dto.DoctorRequest;
import com.clinzo.dto.DoctorResponse;
import com.clinzo.entity.Doctor;
import com.clinzo.exception.BadRequestException;
import com.clinzo.mapper.DoctorMapper;
import com.clinzo.repository.DoctorRepository;
import com.clinzo.util.DateTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    public DoctorService(DoctorRepository doctorRepository, DoctorMapper doctorMapper) {
        this.doctorRepository = doctorRepository;
        this.doctorMapper = doctorMapper;
    }

    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        if (!DateTimeUtil.isValidTimezone(request.timezone())) {
            throw new BadRequestException("Doctor timezone is invalid");
        }

        Doctor doctor = Doctor.builder()
                .name(request.name())
                .timezone(request.timezone())
                .slotDuration(request.slotDuration())
                .bufferTime(request.bufferTime())
                .build();
        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream().map(doctorMapper::toResponse).collect(Collectors.toList());
    }
}

package com.clinzo.controller;

import com.clinzo.dto.DoctorRequest;
import com.clinzo.dto.DoctorResponse;
import com.clinzo.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse createDoctor(@RequestBody @Valid DoctorRequest request) {
        return doctorService.createDoctor(request);
    }

    @GetMapping
    public List<DoctorResponse> getDoctors() {
        return doctorService.getAllDoctors();
    }
}

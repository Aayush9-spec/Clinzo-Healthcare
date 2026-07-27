package com.clinzo.controller;

import com.clinzo.dto.AvailabilityRequest;
import com.clinzo.dto.AvailabilityResponse;
import com.clinzo.dto.SlotResponse;
import com.clinzo.service.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityResponse createAvailability(@RequestBody @Valid AvailabilityRequest request) {
        return availabilityService.createAvailability(request);
    }

    @PutMapping("/{id}")
    public AvailabilityResponse updateAvailability(@PathVariable UUID id, @RequestBody @Valid AvailabilityRequest request) {
        return availabilityService.updateAvailability(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvailability(@PathVariable UUID id) {
        availabilityService.deleteAvailability(id);
    }

    @PostMapping("/{id}/generate-slots")
    public List<SlotResponse> generateSlots(@PathVariable UUID id) {
        return availabilityService.generateSlots(id);
    }
}

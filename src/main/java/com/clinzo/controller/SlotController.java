package com.clinzo.controller;

import com.clinzo.dto.SlotResponse;
import com.clinzo.service.SlotService;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
public class SlotController {
    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping("/{doctorId}/slots")
    public List<SlotResponse> getAvailableSlots(@PathVariable UUID doctorId,
                                                @RequestParam("date") @NotNull LocalDate date) {
        return slotService.getAvailableSlots(doctorId, date);
    }
}

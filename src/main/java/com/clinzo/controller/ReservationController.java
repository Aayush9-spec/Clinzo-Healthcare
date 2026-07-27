package com.clinzo.controller;

import com.clinzo.dto.ReservationResponse;
import com.clinzo.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/slots/{slotId}/hold")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse holdSlot(@PathVariable UUID slotId) {
        return reservationService.holdSlot(slotId);
    }
}

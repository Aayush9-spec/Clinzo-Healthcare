package com.clinzo.controller;

import com.clinzo.dto.BookingRequest;
import com.clinzo.dto.BookingResponse;
import com.clinzo.dto.RescheduleRequest;
import com.clinzo.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/slots/{slotId}/book")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookSlot(@PathVariable UUID slotId, @RequestBody @Valid BookingRequest request) {
        return bookingService.bookSlot(slotId, request);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public BookingResponse cancelBooking(@PathVariable UUID bookingId) {
        return bookingService.cancelBooking(bookingId);
    }

    @PostMapping("/bookings/{bookingId}/reschedule")
    public BookingResponse rescheduleBooking(@PathVariable UUID bookingId, @RequestBody @Valid RescheduleRequest request) {
        return bookingService.rescheduleBooking(bookingId, request);
    }
}

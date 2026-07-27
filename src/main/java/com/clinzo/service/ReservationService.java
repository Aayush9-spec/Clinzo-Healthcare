package com.clinzo.service;

import com.clinzo.dto.ReservationResponse;
import com.clinzo.entity.ReservationHold;
import com.clinzo.entity.ReservationStatus;
import com.clinzo.entity.Slot;
import com.clinzo.entity.SlotStatus;
import com.clinzo.exception.ConflictException;
import com.clinzo.exception.ResourceNotFoundException;
import com.clinzo.mapper.ReservationMapper;
import com.clinzo.repository.ReservationHoldRepository;
import com.clinzo.repository.SlotRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private static final Duration HOLD_DURATION = Duration.ofMinutes(2);

    private final SlotRepository slotRepository;
    private final ReservationHoldRepository reservationHoldRepository;
    private final ReservationMapper reservationMapper;
    private final AuditLogService auditLogService;

    public ReservationService(SlotRepository slotRepository,
                              ReservationHoldRepository reservationHoldRepository,
                              ReservationMapper reservationMapper,
                              AuditLogService auditLogService) {
        this.slotRepository = slotRepository;
        this.reservationHoldRepository = reservationHoldRepository;
        this.reservationMapper = reservationMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ReservationResponse holdSlot(UUID slotId) {
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new ConflictException("Slot cannot be held because it is not available");
        }
        slot.setStatus(SlotStatus.HELD);
        ReservationHold hold = ReservationHold.builder()
                .slot(slot)
                .expiresAt(Instant.now().plus(HOLD_DURATION))
                .status(ReservationStatus.ACTIVE)
                .build();
        ReservationHold saved = reservationHoldRepository.save(hold);
        auditLogService.record("HOLD_SLOT", "ReservationHold", saved.getId().toString(), "slotId=" + slotId);
        return reservationMapper.toResponse(saved);
    }

    @Scheduled(fixedDelayString = "PT30S")
    @Transactional
    public void expireHolds() {
        List<ReservationHold> expiredHolds = reservationHoldRepository.findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, Instant.now());
        expiredHolds.forEach(hold -> {
            hold.setStatus(ReservationStatus.EXPIRED);
            Slot slot = hold.getSlot();
            if (slot.getStatus() == SlotStatus.HELD) {
                slot.setStatus(SlotStatus.AVAILABLE);
            }
            auditLogService.record("EXPIRE_HOLD", "ReservationHold", hold.getId().toString(), "slotId=" + slot.getId());
        });
        reservationHoldRepository.saveAll(expiredHolds);
    }
}

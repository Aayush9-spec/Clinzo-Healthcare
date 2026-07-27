package com.clinzo.mapper;

import com.clinzo.dto.ReservationResponse;
import com.clinzo.entity.ReservationHold;
import com.clinzo.util.DateTimeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = MapStructConfig.class, imports = DateTimeUtil.class)
public interface ReservationMapper {
    @Mapping(target = "slotId", source = "slot.id")
    @Mapping(target = "expiresAt", expression = "java(DateTimeUtil.toUtcZonedDateTime(reservationHold.getExpiresAt()))")
    ReservationResponse toResponse(ReservationHold reservationHold);
}

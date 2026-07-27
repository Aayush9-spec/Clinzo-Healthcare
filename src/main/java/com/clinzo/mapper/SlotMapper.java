package com.clinzo.mapper;

import com.clinzo.dto.SlotResponse;
import com.clinzo.entity.Slot;
import com.clinzo.util.DateTimeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = MapStructConfig.class, imports = DateTimeUtil.class)
public interface SlotMapper {
    @Mapping(target = "doctorId", source = "doctor.id")
    @Mapping(target = "availabilityId", source = "availability.id")
    @Mapping(target = "startTime", expression = "java(DateTimeUtil.toDoctorZone(slot.getStartTime(), slot.getDoctor().getTimezone()))")
    @Mapping(target = "endTime", expression = "java(DateTimeUtil.toDoctorZone(slot.getEndTime(), slot.getDoctor().getTimezone()))")
    SlotResponse toResponse(Slot slot);
}

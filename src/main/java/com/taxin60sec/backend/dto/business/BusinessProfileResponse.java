package com.taxin60sec.backend.dto.business;
import com.taxin60sec.backend.entity.enums.*; import java.time.*;
public record BusinessProfileResponse(Long id,Long clientProfileId,String businessName,BusinessType businessType,String panNumber,String gstin,String tanNumber,String cin,String msmeNumber,LocalDate incorporationDate,BusinessStatus businessStatus,Long assignedCaId,String address,Instant createdAt,Instant updatedAt){}

package com.taxin60sec.backend.service;
import com.taxin60sec.backend.dto.business.*; import java.util.List;
public interface BusinessProfileService { BusinessProfileResponse createBusiness(BusinessProfileRequest request); BusinessProfileResponse updateBusiness(Long id,BusinessProfileRequest request); void deleteBusiness(Long id); BusinessProfileResponse getBusiness(Long id); List<BusinessProfileResponse> getBusinessesForClient(Long clientId); }

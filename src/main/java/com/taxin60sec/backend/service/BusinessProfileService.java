package com.taxin60sec.backend.service;
import com.taxin60sec.backend.dto.business.*; import java.util.List;
public interface BusinessProfileService {
    BusinessProfileResponse createBusiness(BusinessProfileRequest request);
    BusinessProfileResponse updateBusiness(Long id,BusinessProfileRequest request);
    void deleteBusiness(Long id);
    BusinessProfileResponse getBusiness(Long id);
    List<BusinessProfileResponse> getBusinessesForClient(Long clientId);

    BusinessProfileResponse createOwnBusiness(Long requestingUserId, BusinessProfileRequest request);
    List<BusinessProfileResponse> getOwnBusinesses(Long requestingUserId);
    BusinessProfileResponse getOwnBusiness(Long requestingUserId, Long businessId);
    BusinessProfileResponse updateOwnBusiness(Long requestingUserId, Long businessId, BusinessProfileRequest request);
    void deleteOwnBusiness(Long requestingUserId, Long businessId);

    CommandCenterResponse commandCenterForUser(Long requestingUserId);
}
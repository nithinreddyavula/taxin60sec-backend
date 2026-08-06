package com.taxin60sec.backend.service.impl;
import com.taxin60sec.backend.common.ApiErrorCode; import com.taxin60sec.backend.dto.business.*; import com.taxin60sec.backend.entity.*; import com.taxin60sec.backend.exception.ApiException; import com.taxin60sec.backend.mapper.BusinessProfileMapper; import com.taxin60sec.backend.repository.*; import com.taxin60sec.backend.service.BusinessProfileService; import jakarta.transaction.Transactional; import org.springframework.http.HttpStatus; import java.util.List; import org.springframework.stereotype.Service;

@Service @Transactional public class BusinessProfileServiceImpl implements BusinessProfileService {

    private final BusinessProfileRepository businesses;
    private final ClientProfileRepository clients;
    private final UserRepository users;
    private final BusinessProfileMapper mapper;

    public BusinessProfileServiceImpl(BusinessProfileRepository businesses,ClientProfileRepository clients,UserRepository users,BusinessProfileMapper mapper){this.businesses=businesses;this.clients=clients;this.users=users;this.mapper=mapper;}

    public BusinessProfileResponse createBusiness(BusinessProfileRequest r){
        if (r.clientProfileId() == null) throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "clientProfileId is required");
        BusinessProfile b=new BusinessProfile();apply(b,r);return mapper.toResponse(businesses.save(b));
    }
    public BusinessProfileResponse updateBusiness(Long id,BusinessProfileRequest r){BusinessProfile b=find(id);apply(b,r);return mapper.toResponse(b);}
    public void deleteBusiness(Long id){find(id).markDeleted();}
    public BusinessProfileResponse getBusiness(Long id){return mapper.toResponse(find(id));}
    public List<BusinessProfileResponse> getBusinessesForClient(Long clientId){return businesses.findByClientProfileIdAndDeletedFalse(clientId).stream().map(mapper::toResponse).toList();}

    public BusinessProfileResponse createOwnBusiness(Long requestingUserId, BusinessProfileRequest r) {
        ClientProfile own = ownClientProfile(requestingUserId);
        BusinessProfile b = new BusinessProfile();
        b.setClientProfile(own);
        b.setBusinessName(r.businessName());
        b.setBusinessType(r.businessType());
        b.setPanNumber(r.panNumber());
        b.setGstin(r.gstin());
        b.setTanNumber(r.tanNumber());
        b.setCin(r.cin());
        b.setMsmeNumber(r.msmeNumber());
        b.setIncorporationDate(r.incorporationDate());
        b.setBusinessStatus(r.businessStatus());
        b.setAddress(r.address());
        return mapper.toResponse(businesses.save(b));
    }

    public List<BusinessProfileResponse> getOwnBusinesses(Long requestingUserId) {
        ClientProfile own = ownClientProfile(requestingUserId);
        return businesses.findByClientProfileIdAndDeletedFalse(own.getId()).stream().map(mapper::toResponse).toList();
    }

    public BusinessProfileResponse getOwnBusiness(Long requestingUserId, Long businessId) {
        return mapper.toResponse(findOwned(requestingUserId, businessId));
    }

    public BusinessProfileResponse updateOwnBusiness(Long requestingUserId, Long businessId, BusinessProfileRequest r) {
        BusinessProfile b = findOwned(requestingUserId, businessId);
        b.setBusinessName(r.businessName());
        b.setBusinessType(r.businessType());
        b.setPanNumber(r.panNumber());
        b.setGstin(r.gstin());
        b.setTanNumber(r.tanNumber());
        b.setCin(r.cin());
        b.setMsmeNumber(r.msmeNumber());
        b.setIncorporationDate(r.incorporationDate());
        b.setBusinessStatus(r.businessStatus());
        b.setAddress(r.address());
        return mapper.toResponse(b);
    }

    public void deleteOwnBusiness(Long requestingUserId, Long businessId) {
        findOwned(requestingUserId, businessId).markDeleted();
    }

    public CommandCenterResponse commandCenterForUser(Long requestingUserId) {
        ClientProfile own = ownClientProfile(requestingUserId);
        List<BusinessProfile> entities = businesses.findByClientProfileIdAndDeletedFalse(own.getId());

        List<CommandCenterResponse.EntitySummary> summaries = entities.stream()
                .map(b -> new CommandCenterResponse.EntitySummary(
                        b.getId(),
                        b.getBusinessName(),
                        b.getBusinessType(),
                        b.getBusinessStatus(),
                        b.getPanNumber(),
                        b.getGstin(),
                        b.getIncorporationDate(),
                        b.getAssignedCA() != null ? b.getAssignedCA().getId() : null,
                        b.getAssignedCA() != null ? b.getAssignedCA().getFullName() : null
                ))
                .toList();

        return new CommandCenterResponse(summaries.size(), summaries);
    }

    private ClientProfile ownClientProfile(Long requestingUserId) {
        return clients.findByUserId(requestingUserId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Client profile not found"));
    }

    private BusinessProfile findOwned(Long requestingUserId, Long businessId) {
        BusinessProfile b = find(businessId);
        Long ownerUserId = b.getClientProfile() != null && b.getClientProfile().getUser() != null
                ? b.getClientProfile().getUser().getId()
                : null;
        if (ownerUserId == null || !ownerUserId.equals(requestingUserId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Business profile not found");
        }
        return b;
    }

    private BusinessProfile find(Long id){return businesses.findById(id).filter(b->!b.isDeleted()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,ApiErrorCode.NOT_FOUND,"Business profile not found"));}

    private void apply(BusinessProfile b,BusinessProfileRequest r){
        b.setClientProfile(clients.findById(r.clientProfileId()).filter(c->!c.isDeleted()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,ApiErrorCode.NOT_FOUND,"Client profile not found")));
        if (r.assignedCaId() != null) {
            b.setAssignedCA(users.findById(r.assignedCaId()).filter(u->!u.isDeleted()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,ApiErrorCode.NOT_FOUND,"Assigned CA not found")));
        }
        b.setBusinessName(r.businessName());b.setBusinessType(r.businessType());b.setPanNumber(r.panNumber());b.setGstin(r.gstin());b.setTanNumber(r.tanNumber());b.setCin(r.cin());b.setMsmeNumber(r.msmeNumber());b.setIncorporationDate(r.incorporationDate());b.setBusinessStatus(r.businessStatus());b.setAddress(r.address());
    }
}
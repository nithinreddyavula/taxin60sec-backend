package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.business.BusinessProfileRequest;
import com.taxin60sec.backend.dto.business.BusinessProfileResponse;
import com.taxin60sec.backend.entity.BusinessProfile;
import com.taxin60sec.backend.entity.ClientProfile;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.BusinessStatus;
import com.taxin60sec.backend.entity.enums.BusinessType;
import com.taxin60sec.backend.mapper.BusinessProfileMapper;
import com.taxin60sec.backend.repository.BusinessProfileRepository;
import com.taxin60sec.backend.repository.ClientProfileRepository;
import com.taxin60sec.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusinessProfileServiceImplTest {
    @Test
    void createsBusinessForExistingClientAndCa() {
        BusinessProfileRepository businesses = mock(BusinessProfileRepository.class);
        ClientProfileRepository clients = mock(ClientProfileRepository.class);
        UserRepository users = mock(UserRepository.class);
        BusinessProfileServiceImpl service = new BusinessProfileServiceImpl(businesses, clients, users, new BusinessProfileMapper());
        ClientProfile client = new ClientProfile(); client.setId(10L);
        User ca = new User(); ca.setId(20L);
        when(clients.findById(10L)).thenReturn(Optional.of(client));
        when(users.findById(20L)).thenReturn(Optional.of(ca));
        when(businesses.save(any(BusinessProfile.class))).thenAnswer(invocation -> { BusinessProfile profile = invocation.getArgument(0); profile.setId(30L); return profile; });

        BusinessProfileRequest request = new BusinessProfileRequest(10L, "Tax60 Pvt Ltd", BusinessType.PRIVATE_LIMITED, "ABCDE1234F", null, null, null, null, null, BusinessStatus.ACTIVE, 20L, "Pune");
        BusinessProfileResponse response = service.createBusiness(request);

        assertEquals(30L, response.id());
        assertEquals(10L, response.clientProfileId());
        assertEquals(20L, response.assignedCaId());
        verify(businesses).save(any(BusinessProfile.class));
    }
}

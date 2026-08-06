package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.ServiceOffering;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.UserRepository;
import com.taxin60sec.backend.service.PlatformSettingsService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PricingServiceTest {

    private PricingService newService(
            CaseRepository cases,
            UserRepository users,
            PlatformSettingsService settingsService) {

        when(settingsService.getDecimal(
                PlatformSettingsService.REFERRAL_DISCOUNT_PERCENTAGE))
                .thenReturn(new BigDecimal("20"));

        return new PricingService(cases, users, settingsService);
    }

    private Case caseWith(User client, boolean discountApplied) {
        ServiceOffering offering = new ServiceOffering();
        offering.setBasePrice(new BigDecimal("1000.00"));

        Case taxCase = new Case();
        taxCase.setId(1L);
        taxCase.setServiceOffering(offering);
        taxCase.setClient(client);
        taxCase.setReferralDiscountApplied(discountApplied);

        return taxCase;
    }

    @Test
    void chargesFullPriceWhenNoReferralInvolved() {
        CaseRepository cases = mock(CaseRepository.class);
        UserRepository users = mock(UserRepository.class);
        PlatformSettingsService settings = mock(PlatformSettingsService.class);

        PricingService service = newService(cases, users, settings);

        User client = new User();
        client.setReferralCredits(0);

        Case taxCase = caseWith(client, false);

        when(cases.findById(1L)).thenReturn(Optional.of(taxCase));

        BigDecimal amount = service.resolveAmount(1L);

        assertEquals(new BigDecimal("1000.00"), amount);
        assertFalse(taxCase.isReferralDiscountApplied());
    }

    @Test
    void appliesWelcomeDiscountOnceForReferredClient() {
        CaseRepository cases = mock(CaseRepository.class);
        UserRepository users = mock(UserRepository.class);
        PlatformSettingsService settings = mock(PlatformSettingsService.class);

        PricingService service = newService(cases, users, settings);

        User client = new User();
        client.setReferredByCode("TXABCDE");

        Case taxCase = caseWith(client, false);

        when(cases.findById(1L)).thenReturn(Optional.of(taxCase));

        BigDecimal amount = service.resolveAmount(1L);

        assertEquals(new BigDecimal("800.00"), amount);
        assertTrue(taxCase.isReferralDiscountApplied());
    }

    @Test
    void doesNotDiscountTwiceForTheSameCase() {
        CaseRepository cases = mock(CaseRepository.class);
        UserRepository users = mock(UserRepository.class);
        PlatformSettingsService settings = mock(PlatformSettingsService.class);

        PricingService service = newService(cases, users, settings);

        User client = new User();
        client.setReferredByCode("TXABCDE");

        Case taxCase = caseWith(client, true);

        when(cases.findById(1L)).thenReturn(Optional.of(taxCase));

        BigDecimal amount = service.resolveAmount(1L);

        assertEquals(new BigDecimal("1000.00"), amount);
    }

    @Test
    void consumesOneReferralCreditWhenAvailable() {
        CaseRepository cases = mock(CaseRepository.class);
        UserRepository users = mock(UserRepository.class);
        PlatformSettingsService settings = mock(PlatformSettingsService.class);

        PricingService service = newService(cases, users, settings);

        User client = new User();
        client.setReferralCredits(2);

        Case taxCase = caseWith(client, false);

        when(cases.findById(1L)).thenReturn(Optional.of(taxCase));
        when(users.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal amount = service.resolveAmount(1L);

        assertEquals(new BigDecimal("800.00"), amount);
        assertEquals(1, client.getReferralCredits());
    }
}
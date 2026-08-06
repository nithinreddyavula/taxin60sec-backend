package com.taxin60sec.backend.service;

import com.taxin60sec.backend.entity.AppSetting;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.repository.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PlatformSettingsService {

    public static final String COMMISSION_PERCENTAGE = "COMMISSION_PERCENTAGE";
    public static final String GST_RATE_PERCENTAGE = "GST_RATE_PERCENTAGE";
    public static final String REFERRAL_DISCOUNT_PERCENTAGE = "REFERRAL_DISCOUNT_PERCENTAGE";

    private record Definition(String defaultValue, String description) {}

    private static final Map<String, Definition> KNOWN_SETTINGS = new LinkedHashMap<>();
    static {
        KNOWN_SETTINGS.put(COMMISSION_PERCENTAGE, new Definition("15",
                "Platform commission taken from each payment at escrow release, as a percentage (15 = 15%)."));
        KNOWN_SETTINGS.put(GST_RATE_PERCENTAGE, new Definition("18",
                "GST rate backed out of the client-facing total at checkout, as a percentage (18 = 18%)."));
        KNOWN_SETTINGS.put(REFERRAL_DISCOUNT_PERCENTAGE, new Definition("20",
                "Discount applied when a client redeems a referral, as a percentage (20 = 20%)."));
    }

    private final AppSettingRepository settings;

    public PlatformSettingsService(AppSettingRepository settings) {
        this.settings = settings;
    }

    public BigDecimal getDecimal(String key) {
        String raw = settings.findByKey(key).map(AppSetting::getValue).orElse(null);
        if (raw == null || raw.isBlank()) {
            raw = defaultValueOf(key);
        }
        return new BigDecimal(raw);
    }

    public BigDecimal getFraction(String key) {
        return getDecimal(key).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    }

    public String getString(String key) {
        return settings.findByKey(key).map(AppSetting::getValue).orElse(defaultValueOf(key));
    }

    public List<com.taxin60sec.backend.dto.admin.PlatformSettingDto> allSettings() {
        return KNOWN_SETTINGS.entrySet().stream()
                .map(e -> {
                    AppSetting stored = settings.findByKey(e.getKey()).orElse(null);
                    return new com.taxin60sec.backend.dto.admin.PlatformSettingDto(
                            e.getKey(),
                            stored != null && stored.getValue() != null ? stored.getValue() : e.getValue().defaultValue(),
                            e.getValue().defaultValue(),
                            e.getValue().description(),
                            stored != null,
                            stored != null ? stored.getUpdatedAt() : null
                    );
                })
                .toList();
    }

    @Transactional
    public com.taxin60sec.backend.dto.admin.PlatformSettingDto update(String key, String value, User admin) {
        Definition def = KNOWN_SETTINGS.get(key);
        if (def == null) {
            throw new com.taxin60sec.backend.exception.ApiException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    com.taxin60sec.backend.common.ApiErrorCode.BAD_REQUEST,
                    "Unknown setting key: " + key);
        }
        try {
            new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new com.taxin60sec.backend.exception.ApiException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    com.taxin60sec.backend.common.ApiErrorCode.BAD_REQUEST,
                    key + " must be a number");
        }

        AppSetting entity = settings.findByKey(key).orElseGet(() -> {
            AppSetting fresh = new AppSetting();
            fresh.setKey(key);
            fresh.setDescription(def.description());
            return fresh;
        });
        entity.setValue(value);
        entity.setUpdatedBy(admin);
        settings.save(entity);

        return new com.taxin60sec.backend.dto.admin.PlatformSettingDto(
                key, value, def.defaultValue(), def.description(), true, Instant.now());
    }

    private String defaultValueOf(String key) {
        Definition def = KNOWN_SETTINGS.get(key);
        if (def == null) {
            throw new IllegalArgumentException("Unknown setting key: " + key);
        }
        return def.defaultValue();
    }
}
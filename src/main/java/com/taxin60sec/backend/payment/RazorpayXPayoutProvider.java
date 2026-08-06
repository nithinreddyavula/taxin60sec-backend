package com.taxin60sec.backend.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxin60sec.backend.entity.enums.PayoutMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "payout.provider.razorpayx.enabled", havingValue = "true")
public class RazorpayXPayoutProvider implements PayoutProvider {

    private final String keyId;
    private final String keySecret;
    private final String payoutAccountNumber;
    private final RestClient client;
    private final ObjectMapper json;

    public RazorpayXPayoutProvider(
            @Value("${payout.razorpayx.key-id:}") String keyId,
            @Value("${payout.razorpayx.key-secret:}") String keySecret,
            @Value("${payout.razorpayx.account-number:}") String payoutAccountNumber,
            ObjectMapper json
    ) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.payoutAccountNumber = payoutAccountNumber;
        this.json = json;
        this.client = RestClient.builder()
                .baseUrl("https://api.razorpay.com/v1")
                .defaultHeaders(h -> h.setBasicAuth(keyId, keySecret))
                .build();
    }

    @Override
    public String name() {
        return "razorpayx";
    }

    @Override
    public PayoutResult payout(PayoutRequest request) {
        if (keyId.isBlank() || keySecret.isBlank() || payoutAccountNumber.isBlank()) {
            return new PayoutResult("MISCONFIGURED", null,
                    "payout.razorpayx.key-id, key-secret, and account-number must all be set", null, null);
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            return new PayoutResult("FAILED", null, "Payout amount must be positive", null, null);
        }

        try {
            String contactId = request.existingContactId() != null
                    ? request.existingContactId()
                    : createContact(request);

            String fundAccountId = request.existingFundAccountId() != null
                    ? request.existingFundAccountId()
                    : createFundAccount(contactId, request);

            JsonNode payout = createPayout(fundAccountId, request);

            return new PayoutResult(
                    payout.path("status").asText("processing"),
                    payout.path("id").asText(),
                    null,
                    contactId,
                    fundAccountId
            );
        } catch (Exception e) {
            return new PayoutResult("FAILED", null, "RazorpayX payout attempt failed: " + e.getMessage(), null, null);
        }
    }

    private String createContact(PayoutRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", request.accountHolderName());
        if (request.email() != null) body.put("email", request.email());
        if (request.phone() != null) body.put("contact", request.phone());
        body.put("type", "vendor");
        body.put("reference_id", request.reference());

        JsonNode node;
        try {
            node = json.readTree(post("/contacts", body));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("RazorpayX contact response was not valid JSON: " + e.getMessage(), e);
        }
        return node.path("id").asText();
    }

    private String createFundAccount(String contactId, PayoutRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contact_id", contactId);

        if (request.method() == PayoutMethod.UPI) {
            body.put("account_type", "vpa");
            body.put("vpa", Map.of("address", request.upiId()));
        } else {
            body.put("account_type", "bank_account");
            body.put("bank_account", Map.of(
                    "name", request.accountHolderName(),
                    "ifsc", request.bankIfsc(),
                    "account_number", request.bankAccountNumber()
            ));
        }

        JsonNode node;
        try {
            node = json.readTree(post("/fund_accounts", body));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("RazorpayX fund account response was not valid JSON: " + e.getMessage(), e);
        }
        return node.path("id").asText();
    }

    private JsonNode createPayout(String fundAccountId, PayoutRequest request) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("account_number", payoutAccountNumber);
    body.put("fund_account_id", fundAccountId);
    body.put("amount", request.amount().movePointRight(2).longValueExact());
    body.put("currency", request.currency() != null ? request.currency() : "INR");
    body.put("mode", request.method() == PayoutMethod.UPI ? "UPI" : "IMPS");
    body.put("purpose", "payout");
    body.put("queue_if_low_balance", true);
    body.put("reference_id", request.reference());

    try {
        return json.readTree(post("/payouts", body));
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
        throw new RuntimeException(
                "RazorpayX payout response was not valid JSON: " + e.getMessage(),
                e
        );
    }
}

    private String post(String path, Map<String, Object> body) {
        try {
            return client.post().uri(path).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("RazorpayX call to " + path + " failed: " + e.getMessage(), e);
        }
    }
}
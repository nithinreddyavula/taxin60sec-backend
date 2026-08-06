package com.taxin60sec.backend.communication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Real masked-calling via Exotel's Connect API (developer.exotel.com) - a widely used
 * India-based telephony provider for exactly this "Uber-style masked call" pattern.
 * Bridges the client's real number and the CA's real number through an Exotel Exophone
 * (the masked/relay number); neither party ever sees the other's actual number.
 *
 * Requires an Exotel account: account SID, API key, API token, and at least one
 * purchased Exophone. Set call.provider.exotel.enabled=true plus the properties below
 * once you have those - until then, StubCallProvider handles requests instead.
 */
@Component
@ConditionalOnProperty(name = "call.provider.exotel.enabled", havingValue = "true")
public class ExotelCallProvider implements CallProvider {

    private final RestClient client;
    private final String exophone;

    public ExotelCallProvider(
            @Value("${call.provider.exotel.sid}") String sid,
            @Value("${call.provider.exotel.api-key}") String apiKey,
            @Value("${call.provider.exotel.api-token}") String apiToken,
            @Value("${call.provider.exotel.exophone}") String exophone,
            @Value("${call.provider.exotel.subdomain:api.exotel.com}") String subdomain
    ) {
        this.exophone = exophone;
        this.client = RestClient.builder()
                .baseUrl("https://" + subdomain + "/v1/Accounts/" + sid)
                .defaultHeaders(h -> h.setBasicAuth(apiKey, apiToken))
                .build();
    }

    @Override
    public String name() {
        return "EXOTEL";
    }

    @Override
    public ConnectResult connect(ConnectRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("From", request.fromNumber());
        form.add("To", request.toNumber());
        form.add("CallerId", exophone);
        form.add("CallType", "trans");

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                    .uri("/Calls/connect.json")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            String providerCallId = null;
            Object callObj = response == null ? null : response.get("Call");
            if (callObj instanceof Map<?, ?> callMap) {
                Object sidVal = callMap.get("Sid");
                providerCallId = sidVal == null ? null : sidVal.toString();
            }

            return new ConnectResult(exophone, providerCallId, providerCallId != null, "Connected via Exotel");
        } catch (Exception ex) {
            return new ConnectResult(exophone, null, false, "Exotel connect failed: " + ex.getMessage());
        }
    }
}
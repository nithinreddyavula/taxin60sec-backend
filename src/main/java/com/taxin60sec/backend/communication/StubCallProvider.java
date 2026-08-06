package com.taxin60sec.backend.communication;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default no-op provider - active whenever call.provider.exotel.enabled is not "true".
 * Records that a call was requested but does not bridge two real numbers or hand out a
 * real masked number. Exists so the app runs cleanly before an Exotel account is set up,
 * without pretending calling works when it doesn't.
 */
@Component
@ConditionalOnProperty(name = "call.provider.exotel.enabled", havingValue = "false", matchIfMissing = true)
public class StubCallProvider implements CallProvider {

    @Override
    public String name() {
        return "STUB";
    }

    @Override
    public ConnectResult connect(ConnectRequest request) {
        return new ConnectResult(null, null, false,
                "No telephony provider configured - set call.provider.exotel.enabled=true and Exotel credentials to enable real masked calling.");
    }
}
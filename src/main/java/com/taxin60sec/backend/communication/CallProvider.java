package com.taxin60sec.backend.communication;

public interface CallProvider {
    String name();
    ConnectResult connect(ConnectRequest request);

    record ConnectRequest(String fromNumber, String toNumber, String callerId) {}
    record ConnectResult(String maskedNumber, String providerCallId, boolean success, String detail) {}
}
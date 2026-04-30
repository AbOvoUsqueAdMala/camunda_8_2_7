package ru.abovousqueadmala.dto;

import java.util.Map;

public record AsyncStubCallbackRequest(
        String requestId,
        String status,
        String message,
        String trackingId,
        Map<String, Object> variables
) {
}

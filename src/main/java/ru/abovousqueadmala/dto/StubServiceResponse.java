package ru.abovousqueadmala.dto;

public record StubServiceResponse(
        String requestId,
        String status,
        String message,
        String trackingId,
        String processedAt
) {
}

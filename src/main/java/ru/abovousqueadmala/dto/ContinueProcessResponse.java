package ru.abovousqueadmala.dto;

public record ContinueProcessResponse(
        long messageKey,
        String messageName,
        String correlationKey,
        String status
) {
}

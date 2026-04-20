package ru.abovousqueadmala.dto;

public record JobTimeoutUpdateResponse(
        long jobKey,
        long timeoutMs,
        String status
) {
}

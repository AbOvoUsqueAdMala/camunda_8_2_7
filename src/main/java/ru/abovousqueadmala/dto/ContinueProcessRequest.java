package ru.abovousqueadmala.dto;

import java.util.Map;

public record ContinueProcessRequest(
        String requestId,
        Map<String, Object> variables
) {
}

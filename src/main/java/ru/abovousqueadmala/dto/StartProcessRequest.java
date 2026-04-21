package ru.abovousqueadmala.dto;

import java.util.Map;

public record StartProcessRequest(
        String processId,
        Map<String, Object> variables
) {
}

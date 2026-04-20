package ru.abovousqueadmala.dto;

public record StartProcessResponse(
        long processDefinitionKey,
        long processInstanceKey,
        int version,
        String bpmnProcessId
) {
}

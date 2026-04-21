package ru.abovousqueadmala.dto;

public record StubServiceRequest(
        String requestId,
        String documentNumber,
        String confirmedBy,
        Boolean approved,
        String workerStatus,
        Long processInstanceKey
) {
}

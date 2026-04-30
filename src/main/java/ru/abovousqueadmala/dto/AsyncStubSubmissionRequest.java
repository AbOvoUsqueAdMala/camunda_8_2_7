package ru.abovousqueadmala.dto;

public record AsyncStubSubmissionRequest(
        String requestId,
        String documentNumber,
        Long customerId,
        Integer amount,
        Long processInstanceKey
) {
}

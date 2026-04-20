package ru.abovousqueadmala.dto;

public record ActiveJobInfo(
        Long key,
        String intent,
        String timestamp,
        Long position,
        String type,
        String worker,
        Long timeout,
        Long deadline,
        Long processInstanceKey,
        String elementId
) {
}

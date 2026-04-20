package ru.abovousqueadmala.dto;

import java.util.List;

public record BulkTimeoutUpdateResponse(
        int found,
        int updated,
        List<Long> keys
) {
}

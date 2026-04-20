package ru.abovousqueadmala.dto;

import java.util.Map;

public record StartProcessRequest(Map<String, Object> variables) {
}

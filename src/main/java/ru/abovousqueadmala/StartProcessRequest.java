package ru.abovousqueadmala;

import lombok.Data;

import java.util.Map;

@Data
public class StartProcessRequest {

    private Map<String, Object> variables;

}
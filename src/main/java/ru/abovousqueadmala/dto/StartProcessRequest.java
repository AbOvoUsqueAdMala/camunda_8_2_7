package ru.abovousqueadmala.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class StartProcessRequest {

    private String processId;
    private final Map<String, Object> variables = new LinkedHashMap<>();

    public String processId() {
        return processId;
    }

    @JsonSetter("processId")
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Map<String, Object> variables() {
        return Collections.unmodifiableMap(variables);
    }

    @JsonSetter("variables")
    public void rejectNestedVariables(Object ignored) {
        throw new IllegalArgumentException(
                "Nested 'variables' object is not supported. Pass process variables as top-level JSON fields."
        );
    }

    @JsonAnySetter
    public void addTopLevelVariable(String name, Object value) {
        variables.put(name, value);
    }
}

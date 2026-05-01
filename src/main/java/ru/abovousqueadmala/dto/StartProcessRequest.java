package ru.abovousqueadmala.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class StartProcessRequest {

    private String processId;
    private final Map<String, Object> nestedVariables = new LinkedHashMap<>();
    private final Map<String, Object> topLevelVariables = new LinkedHashMap<>();

    public String processId() {
        return processId;
    }

    @JsonSetter("processId")
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Map<String, Object> variables() {
        Map<String, Object> mergedVariables = new LinkedHashMap<>(nestedVariables);
        mergedVariables.putAll(topLevelVariables);
        return Collections.unmodifiableMap(mergedVariables);
    }

    @JsonSetter("variables")
    public void setVariables(Map<String, Object> variables) {
        nestedVariables.clear();

        if (variables != null) {
            nestedVariables.putAll(variables);
        }
    }

    @JsonAnySetter
    public void addTopLevelVariable(String name, Object value) {
        topLevelVariables.put(name, value);
    }
}

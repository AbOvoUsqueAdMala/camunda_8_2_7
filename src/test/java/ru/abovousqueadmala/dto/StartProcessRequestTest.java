package ru.abovousqueadmala.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StartProcessRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesFlatPayloadIntoVariables() throws Exception {
        StartProcessRequest request = objectMapper.readValue("""
                {
                  "processId": "demo-process",
                  "requestId": "req-123",
                  "amount": 850,
                  "customerId": 42
                }
                """, StartProcessRequest.class);

        assertThat(request.processId()).isEqualTo("demo-process");
        assertThat(request.variables()).isEqualTo(Map.of(
                "requestId", "req-123",
                "amount", 850,
                "customerId", 42
        ));
    }

    @Test
    void mergesLegacyVariablesObjectWithTopLevelFields() throws Exception {
        StartProcessRequest request = objectMapper.readValue("""
                {
                  "processId": "demo-process",
                  "variables": {
                    "requestId": "req-legacy",
                    "amount": 100
                  },
                  "customerId": 42
                }
                """, StartProcessRequest.class);

        assertThat(request.processId()).isEqualTo("demo-process");
        assertThat(request.variables()).isEqualTo(Map.of(
                "requestId", "req-legacy",
                "amount", 100,
                "customerId", 42
        ));
    }
}

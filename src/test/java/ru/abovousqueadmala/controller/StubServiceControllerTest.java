package ru.abovousqueadmala.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StubServiceController.class)
class StubServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void acceptPayloadReturnsStubResponse() throws Exception {
        mockMvc.perform(post("/api/stub/external-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "req-123",
                                  "documentNumber": "DOC-77",
                                  "confirmedBy": "user-1",
                                  "approved": true,
                                  "workerStatus": "DONE",
                                  "processInstanceKey": 200
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req-123"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.message").value("Stub service processed request"))
                .andExpect(jsonPath("$.trackingId", startsWith("stub-")))
                .andExpect(jsonPath("$.processedAt").exists());
    }
}

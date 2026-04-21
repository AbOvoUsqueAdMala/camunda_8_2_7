package ru.abovousqueadmala.controller;

import java.util.Map;
import ru.abovousqueadmala.dto.ContinueProcessResponse;
import ru.abovousqueadmala.service.ProcessMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessMessageService processMessageService;

    @Test
    void continueProcessPublishesExternalDataMessage() throws Exception {
        given(processMessageService.publishExternalData(org.mockito.ArgumentMatchers.eq("req-123"), anyMap()))
                .willReturn(new ContinueProcessResponse(77L, "external-data-received", "req-123", "PUBLISHED"));

        mockMvc.perform(post("/api/messages/external-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "req-123",
                                  "variables": {
                                    "documentNumber": "DOC-77"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageKey").value(77))
                .andExpect(jsonPath("$.messageName").value("external-data-received"))
                .andExpect(jsonPath("$.correlationKey").value("req-123"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(processMessageService).publishExternalData("req-123", Map.of("documentNumber", "DOC-77"));
    }
}

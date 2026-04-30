package ru.abovousqueadmala.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.abovousqueadmala.dto.AsyncStubCallbackRequest;
import ru.abovousqueadmala.dto.ContinueProcessResponse;
import ru.abovousqueadmala.service.AsyncStubCallbackService;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AsyncStubCallbackController.class)
class AsyncStubCallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AsyncStubCallbackService asyncStubCallbackService;

    @Test
    void completeAsyncRequestPublishesCallbackMessage() throws Exception {
        AsyncStubCallbackRequest request = new AsyncStubCallbackRequest(
                "async-req-123",
                "COMPLETED",
                "External processing finished",
                "stub-1",
                Map.of("callbackResult", "OK")
        );
        given(asyncStubCallbackService.publishCallback(request))
                .willReturn(new ContinueProcessResponse(
                        77L,
                        "async-stub-callback-received",
                        "async-req-123",
                        "PUBLISHED"
                ));

        mockMvc.perform(post("/api/async-stub-callbacks/completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "async-req-123",
                                  "status": "COMPLETED",
                                  "message": "External processing finished",
                                  "trackingId": "stub-1",
                                  "variables": {
                                    "callbackResult": "OK"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageKey").value(77))
                .andExpect(jsonPath("$.messageName").value("async-stub-callback-received"))
                .andExpect(jsonPath("$.correlationKey").value("async-req-123"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(asyncStubCallbackService).publishCallback(request);
    }

    @Test
    void completeAsyncRequestRejectsBlankRequestId() throws Exception {
        mockMvc.perform(post("/api/async-stub-callbacks/completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": " "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}

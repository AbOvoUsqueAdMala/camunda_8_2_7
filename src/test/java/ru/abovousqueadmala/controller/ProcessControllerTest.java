package ru.abovousqueadmala.controller;

import java.util.Collections;
import java.util.Map;
import ru.abovousqueadmala.dto.StartProcessResponse;
import ru.abovousqueadmala.service.ProcessService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessController.class)
class ProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessService processService;

    @Test
    void startProcessPassesVariablesToService() throws Exception {
        given(processService.startProcess(anyMap()))
                .willReturn(new StartProcessResponse(11L, 22L, 3, "demo-process"));

        mockMvc.perform(post("/api/process/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "variables": {
                                    "customerId": 42,
                                    "approved": true
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processDefinitionKey").value(11))
                .andExpect(jsonPath("$.processInstanceKey").value(22))
                .andExpect(jsonPath("$.version").value(3))
                .andExpect(jsonPath("$.bpmnProcessId").value("demo-process"));

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(processService).startProcess(captor.capture());
        assertThat(captor.getValue())
                .containsEntry("customerId", 42)
                .containsEntry("approved", true);
    }

    @Test
    void startProcessUsesEmptyVariablesWhenBodyIsMissing() throws Exception {
        given(processService.startProcess(Collections.emptyMap()))
                .willReturn(new StartProcessResponse(1L, 2L, 1, "demo-process"));

        mockMvc.perform(post("/api/process/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processInstanceKey").value(2));

        verify(processService).startProcess(Collections.emptyMap());
    }
}

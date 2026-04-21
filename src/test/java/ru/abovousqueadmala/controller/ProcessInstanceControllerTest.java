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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessInstanceController.class)
class ProcessInstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessService processService;

    @Test
    void startProcessPassesVariablesToService() throws Exception {
        given(processService.startProcess(isNull(), anyMap()))
                .willReturn(new StartProcessResponse(11L, 22L, 3, "demo-process"));

        mockMvc.perform(post("/api/process-instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "variables": {
                                    "amount": 42,
                                    "customerId": 7
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processDefinitionKey").value(11))
                .andExpect(jsonPath("$.processInstanceKey").value(22))
                .andExpect(jsonPath("$.version").value(3))
                .andExpect(jsonPath("$.bpmnProcessId").value("demo-process"));

        ArgumentCaptor<String> processIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(processService).startProcess(processIdCaptor.capture(), variablesCaptor.capture());

        assertThat(processIdCaptor.getValue()).isNull();
        assertThat(variablesCaptor.getValue())
                .containsEntry("amount", 42)
                .containsEntry("customerId", 7);
    }

    @Test
    void startProcessUsesRequestedProcessIdWhenProvided() throws Exception {
        given(processService.startProcess(org.mockito.ArgumentMatchers.eq("demo-process"), anyMap()))
                .willReturn(new StartProcessResponse(11L, 22L, 3, "demo-process"));

        mockMvc.perform(post("/api/process-instances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "processId": "demo-process",
                                  "variables": {
                                    "amount": 250
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bpmnProcessId").value("demo-process"));

        verify(processService).startProcess("demo-process", Map.of("amount", 250));
    }

    @Test
    void startProcessUsesEmptyVariablesWhenBodyIsMissing() throws Exception {
        given(processService.startProcess(null, Collections.emptyMap()))
                .willReturn(new StartProcessResponse(1L, 2L, 1, "demo-process"));

        mockMvc.perform(post("/api/process-instances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processInstanceKey").value(2));

        verify(processService).startProcess(null, Collections.emptyMap());
    }
}

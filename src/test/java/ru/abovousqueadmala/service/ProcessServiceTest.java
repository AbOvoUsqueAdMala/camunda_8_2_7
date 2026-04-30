package ru.abovousqueadmala.service;

import java.util.Map;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.CreateProcessInstanceCommandStep1;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import ru.abovousqueadmala.config.AppProperties;
import ru.abovousqueadmala.dto.StartProcessResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

    @Mock
    private ZeebeClient zeebeClient;

    @Mock
    private CreateProcessInstanceCommandStep1 createProcessInstanceCommandStep1;

    @Mock
    private CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep2 createProcessInstanceCommandStep2;

    @Mock
    private CreateProcessInstanceCommandStep1.CreateProcessInstanceCommandStep3 createProcessInstanceCommandStep3;

    @Mock
    private ZeebeFuture<ProcessInstanceEvent> zeebeFuture;

    @Mock
    private ProcessInstanceEvent processInstanceEvent;

    private ProcessService processService;

    @BeforeEach
    void setUp() {
        processService = new ProcessService(zeebeClient, appProperties());
    }

    @Test
    void startProcessCreatesLatestProcessInstanceWithDefaultProcessId() {
        Map<String, Object> variables = Map.of("customerId", 42, "approved", true);

        when(zeebeClient.newCreateInstanceCommand()).thenReturn(createProcessInstanceCommandStep1);
        when(createProcessInstanceCommandStep1.bpmnProcessId("demo-process"))
                .thenReturn(createProcessInstanceCommandStep2);
        when(createProcessInstanceCommandStep2.latestVersion()).thenReturn(createProcessInstanceCommandStep3);
        when(createProcessInstanceCommandStep3.variables(variables)).thenReturn(createProcessInstanceCommandStep3);
        when(createProcessInstanceCommandStep3.send()).thenReturn(zeebeFuture);
        when(zeebeFuture.join()).thenReturn(processInstanceEvent);
        when(processInstanceEvent.getProcessDefinitionKey()).thenReturn(11L);
        when(processInstanceEvent.getProcessInstanceKey()).thenReturn(22L);
        when(processInstanceEvent.getVersion()).thenReturn(3);
        when(processInstanceEvent.getBpmnProcessId()).thenReturn("demo-process");

        StartProcessResponse response = processService.startProcess(variables);

        assertThat(response).isEqualTo(new StartProcessResponse(11L, 22L, 3, "demo-process"));
        verify(createProcessInstanceCommandStep3).variables(variables);
        verify(zeebeFuture).join();
    }

    @Test
    void startProcessUsesExplicitProcessIdWhenProvided() {
        Map<String, Object> variables = Map.of("amount", 150);

        when(zeebeClient.newCreateInstanceCommand()).thenReturn(createProcessInstanceCommandStep1);
        when(createProcessInstanceCommandStep1.bpmnProcessId("another-process"))
                .thenReturn(createProcessInstanceCommandStep2);
        when(createProcessInstanceCommandStep2.latestVersion()).thenReturn(createProcessInstanceCommandStep3);
        when(createProcessInstanceCommandStep3.variables(variables)).thenReturn(createProcessInstanceCommandStep3);
        when(createProcessInstanceCommandStep3.send()).thenReturn(zeebeFuture);
        when(zeebeFuture.join()).thenReturn(processInstanceEvent);
        when(processInstanceEvent.getProcessDefinitionKey()).thenReturn(111L);
        when(processInstanceEvent.getProcessInstanceKey()).thenReturn(222L);
        when(processInstanceEvent.getVersion()).thenReturn(4);
        when(processInstanceEvent.getBpmnProcessId()).thenReturn("another-process");

        StartProcessResponse response = processService.startProcess("another-process", variables);

        assertThat(response).isEqualTo(new StartProcessResponse(111L, 222L, 4, "another-process"));
        verify(createProcessInstanceCommandStep1).bpmnProcessId("another-process");
        verify(createProcessInstanceCommandStep3).variables(variables);
        verify(zeebeFuture).join();
    }

    private static AppProperties appProperties() {
        return new AppProperties(
                new AppProperties.Camunda(
                        "jobs-*",
                        new AppProperties.Zeebe("demo-process")
                ),
                new AppProperties.Elastic("http://localhost:9200"),
                new AppProperties.StubService(
                        "http://localhost:8080",
                        "/api/stub/external-service",
                        "/api/stub/external-service/async"
                )
        );
    }
}

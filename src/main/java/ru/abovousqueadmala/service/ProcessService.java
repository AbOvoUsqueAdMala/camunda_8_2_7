package ru.abovousqueadmala.service;

import java.util.Map;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import ru.abovousqueadmala.config.AppProperties;
import ru.abovousqueadmala.dto.StartProcessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ZeebeClient zeebeClient;
    private final AppProperties appProperties;

    public StartProcessResponse startProcess(Map<String, Object> variables) {
        return startProcess(null, variables);
    }

    public StartProcessResponse startProcess(String processId, Map<String, Object> variables) {
        String defaultProcessId = appProperties.camunda().zeebe().processId();
        String resolvedProcessId = processId != null && !processId.isBlank()
                ? processId
                : defaultProcessId;

        ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(resolvedProcessId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        return new StartProcessResponse(
                event.getProcessDefinitionKey(),
                event.getProcessInstanceKey(),
                event.getVersion(),
                event.getBpmnProcessId()
        );
    }
}

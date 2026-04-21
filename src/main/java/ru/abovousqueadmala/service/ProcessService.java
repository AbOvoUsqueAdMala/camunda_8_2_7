package ru.abovousqueadmala.service;

import java.util.Map;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import ru.abovousqueadmala.dto.StartProcessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProcessService {

    private final ZeebeClient zeebeClient;
    private final String defaultProcessId;

    public ProcessService(
            ZeebeClient zeebeClient,
            @Value("${app.camunda.zeebe.process-id}") String defaultProcessId
    ) {
        this.zeebeClient = zeebeClient;
        this.defaultProcessId = defaultProcessId;
    }

    public StartProcessResponse startProcess(Map<String, Object> variables) {
        return startProcess(null, variables);
    }

    public StartProcessResponse startProcess(String processId, Map<String, Object> variables) {
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

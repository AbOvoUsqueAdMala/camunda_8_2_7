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
    private final String processId;

    public ProcessService(
            ZeebeClient zeebeClient,
            @Value("${app.camunda.zeebe.process-id}") String processId
    ) {
        this.zeebeClient = zeebeClient;
        this.processId = processId;
    }

    public StartProcessResponse startProcess(Map<String, Object> variables) {
        ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(processId)
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

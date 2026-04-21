package ru.abovousqueadmala.service;

import java.util.Collections;
import java.util.Map;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import ru.abovousqueadmala.dto.ContinueProcessResponse;
import org.springframework.stereotype.Service;

@Service
public class ProcessMessageService {

    static final String EXTERNAL_DATA_MESSAGE_NAME = "external-data-received";
    static final String PUBLISHED_STATUS = "PUBLISHED";

    private final ZeebeClient zeebeClient;

    public ProcessMessageService(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    public ContinueProcessResponse publishExternalData(String requestId, Map<String, Object> variables) {
        Map<String, Object> payload = variables != null ? variables : Collections.emptyMap();

        PublishMessageResponse response = zeebeClient.newPublishMessageCommand()
                .messageName(EXTERNAL_DATA_MESSAGE_NAME)
                .correlationKey(requestId)
                .variables(payload)
                .send()
                .join();

        return new ContinueProcessResponse(
                response.getMessageKey(),
                EXTERNAL_DATA_MESSAGE_NAME,
                requestId,
                PUBLISHED_STATUS
        );
    }
}

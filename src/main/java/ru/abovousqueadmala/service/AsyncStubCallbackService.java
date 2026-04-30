package ru.abovousqueadmala.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.abovousqueadmala.dto.AsyncStubCallbackRequest;
import ru.abovousqueadmala.dto.ContinueProcessResponse;

@Service
@RequiredArgsConstructor
public class AsyncStubCallbackService {

    static final String MESSAGE_NAME = "async-stub-callback-received";
    static final String PUBLISHED_STATUS = "PUBLISHED";

    private final ZeebeClient zeebeClient;

    public ContinueProcessResponse publishCallback(AsyncStubCallbackRequest request) {
        String requestId = requireRequestId(request);
        Map<String, Object> payload = buildPayload(request);

        PublishMessageResponse response = zeebeClient.newPublishMessageCommand()
                .messageName(MESSAGE_NAME)
                .correlationKey(requestId)
                .variables(payload)
                .send()
                .join();

        return new ContinueProcessResponse(
                response.getMessageKey(),
                MESSAGE_NAME,
                requestId,
                PUBLISHED_STATUS
        );
    }

    private static String requireRequestId(AsyncStubCallbackRequest request) {
        if (request == null || request.requestId() == null || request.requestId().isBlank()) {
            throw new IllegalArgumentException("requestId is required");
        }

        return request.requestId();
    }

    private static Map<String, Object> buildPayload(AsyncStubCallbackRequest request) {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> additionalVariables = request.variables() != null
                ? request.variables()
                : Collections.emptyMap();

        payload.putAll(additionalVariables);

        putIfPresent(payload, "asyncStubCallbackStatus", request.status());
        putIfPresent(payload, "asyncStubCallbackMessage", request.message());
        putIfPresent(payload, "asyncStubCallbackTrackingId", request.trackingId());

        return payload;
    }

    private static void putIfPresent(Map<String, Object> payload, String key, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(key, value);
        }
    }
}

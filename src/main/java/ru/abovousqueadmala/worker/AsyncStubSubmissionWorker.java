package ru.abovousqueadmala.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.abovousqueadmala.dto.AsyncStubSubmissionRequest;
import ru.abovousqueadmala.dto.StubServiceResponse;
import ru.abovousqueadmala.service.StubServiceClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class AsyncStubSubmissionWorker {

    private final StubServiceClient stubServiceClient;

    @JobWorker(type = "async-stub-submission-task")
    public Map<String, Object> handleJob(final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String requestId = requireString(variables.get("requestId"), "requestId");

        AsyncStubSubmissionRequest request = new AsyncStubSubmissionRequest(
                requestId,
                asString(variables.get("documentNumber")),
                asLong(variables.get("customerId")),
                asInteger(variables.get("amount")),
                job.getProcessInstanceKey()
        );

        log.info(
                "Submitting async request to stub service. jobKey={}, processInstanceKey={}, requestId={}",
                job.getKey(),
                job.getProcessInstanceKey(),
                requestId
        );

        StubServiceResponse response = stubServiceClient.submitAsyncRequest(request);

        Map<String, Object> resultVars = new HashMap<>();
        resultVars.put("asyncStubSubmissionStatus", response.status());
        resultVars.put("asyncStubSubmissionMessage", response.message());
        resultVars.put("asyncStubTrackingId", response.trackingId());
        resultVars.put("asyncStubAcceptedAt", response.processedAt());

        log.info(
                "Async request accepted by stub service. jobKey={}, trackingId={}, status={}",
                job.getKey(),
                response.trackingId(),
                response.status()
        );
        return resultVars;
    }

    private static String requireString(Object value, String fieldName) {
        String text = asString(value);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return text;
    }

    private static String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private static Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        return value != null ? Long.valueOf(String.valueOf(value)) : null;
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        return value != null ? Integer.valueOf(String.valueOf(value)) : null;
    }
}

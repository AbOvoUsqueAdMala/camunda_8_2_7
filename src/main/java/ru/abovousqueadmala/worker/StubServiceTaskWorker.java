package ru.abovousqueadmala.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.abovousqueadmala.dto.StubServiceRequest;
import ru.abovousqueadmala.dto.StubServiceResponse;
import ru.abovousqueadmala.service.StubServiceClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class StubServiceTaskWorker {

    private final StubServiceClient stubServiceClient;

    @JobWorker(type = "stub-service-task")
    public Map<String, Object> handleJob(final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        StubServiceRequest request = new StubServiceRequest(
                asString(variables.get("requestId")),
                asString(variables.get("documentNumber")),
                asString(variables.get("confirmedBy")),
                asBoolean(variables.get("approved")),
                asString(variables.get("workerStatus")),
                job.getProcessInstanceKey()
        );

        log.info(
                "Calling stub service. jobKey={}, processInstanceKey={}, requestId={}",
                job.getKey(),
                job.getProcessInstanceKey(),
                request.requestId()
        );

        StubServiceResponse response = stubServiceClient.sendApprovalData(request);

        Map<String, Object> resultVars = new HashMap<>();
        resultVars.put("stubServiceStatus", response.status());
        resultVars.put("stubServiceMessage", response.message());
        resultVars.put("stubServiceTrackingId", response.trackingId());
        resultVars.put("stubServiceProcessedAt", response.processedAt());

        log.info(
                "Stub service response accepted. jobKey={}, trackingId={}, status={}",
                job.getKey(),
                response.trackingId(),
                response.status()
        );
        return resultVars;
    }

    private static String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private static Boolean asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return value != null ? Boolean.valueOf(String.valueOf(value)) : null;
    }
}

package ru.abovousqueadmala.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.time.OffsetDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.abovousqueadmala.dto.StubServiceRequest;
import ru.abovousqueadmala.dto.StubServiceResponse;
import ru.abovousqueadmala.service.StubServiceClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StubServiceTaskWorkerTest {

    @Test
    void handleJobSendsVariablesToStubServiceAndReturnsResponseVariables() {
        StubServiceClient stubServiceClient = mock(StubServiceClient.class);
        StubServiceTaskWorker stubServiceTaskWorker = new StubServiceTaskWorker(stubServiceClient);
        ActivatedJob activatedJob = mock(ActivatedJob.class);

        when(activatedJob.getKey()).thenReturn(100L);
        when(activatedJob.getProcessInstanceKey()).thenReturn(200L);
        when(activatedJob.getVariablesAsMap()).thenReturn(Map.of(
                "requestId", "req-123",
                "documentNumber", "DOC-77",
                "confirmedBy", "user-1",
                "approved", true,
                "workerStatus", "DONE"
        ));

        when(stubServiceClient.sendApprovalData(new StubServiceRequest(
                "req-123",
                "DOC-77",
                "user-1",
                true,
                "DONE",
                200L
        ))).thenReturn(new StubServiceResponse(
                "req-123",
                "ACCEPTED",
                "Stub service processed request",
                "stub-1",
                OffsetDateTime.now().toString()
        ));

        Map<String, Object> result = stubServiceTaskWorker.handleJob(activatedJob);

        assertThat(result).containsEntry("stubServiceStatus", "ACCEPTED");
        assertThat(result).containsEntry("stubServiceMessage", "Stub service processed request");
        assertThat(result).containsEntry("stubServiceTrackingId", "stub-1");
        assertThat(OffsetDateTime.parse((String) result.get("stubServiceProcessedAt"))).isNotNull();

        verify(stubServiceClient).sendApprovalData(new StubServiceRequest(
                "req-123",
                "DOC-77",
                "user-1",
                true,
                "DONE",
                200L
        ));
    }
}

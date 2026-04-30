package ru.abovousqueadmala.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ru.abovousqueadmala.dto.AsyncStubSubmissionRequest;
import ru.abovousqueadmala.dto.StubServiceResponse;
import ru.abovousqueadmala.service.StubServiceClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsyncStubSubmissionWorkerTest {

    @Test
    void handleJobSubmitsAsyncRequestAndReturnsAcceptanceVariables() {
        StubServiceClient stubServiceClient = mock(StubServiceClient.class);
        AsyncStubSubmissionWorker worker = new AsyncStubSubmissionWorker(stubServiceClient);
        ActivatedJob activatedJob = mock(ActivatedJob.class);

        when(activatedJob.getKey()).thenReturn(100L);
        when(activatedJob.getProcessInstanceKey()).thenReturn(200L);
        when(activatedJob.getVariablesAsMap()).thenReturn(Map.of(
                "requestId", "async-req-123",
                "documentNumber", "DOC-9001",
                "customerId", 42L,
                "amount", 850
        ));

        when(stubServiceClient.submitAsyncRequest(new AsyncStubSubmissionRequest(
                "async-req-123",
                "DOC-9001",
                42L,
                850,
                200L
        ))).thenReturn(new StubServiceResponse(
                "async-req-123",
                "ACCEPTED",
                "Stub service accepted request",
                "stub-1",
                "2026-04-30T12:00:00Z"
        ));

        Map<String, Object> result = worker.handleJob(activatedJob);

        assertThat(result).containsEntry("asyncStubSubmissionStatus", "ACCEPTED");
        assertThat(result).containsEntry("asyncStubSubmissionMessage", "Stub service accepted request");
        assertThat(result).containsEntry("asyncStubTrackingId", "stub-1");
        assertThat(result).containsEntry("asyncStubAcceptedAt", "2026-04-30T12:00:00Z");

        verify(stubServiceClient).submitAsyncRequest(new AsyncStubSubmissionRequest(
                "async-req-123",
                "DOC-9001",
                42L,
                850,
                200L
        ));
    }
}

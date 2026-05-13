package ru.abovousqueadmala.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.abovousqueadmala.dto.AsyncStubSubmissionRequest;
import ru.abovousqueadmala.dto.StubServiceResponse;
import ru.abovousqueadmala.service.StubServiceClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
                "documentNumber", "DOC-9001",
                "customerId", 42L,
                "amount", 850
        ));

        when(stubServiceClient.submitAsyncRequest(any(AsyncStubSubmissionRequest.class)))
                .thenAnswer(invocation -> {
                    AsyncStubSubmissionRequest request = invocation.getArgument(0);
                    return new StubServiceResponse(
                            request.requestId(),
                            "ACCEPTED",
                            "Stub service accepted request",
                            "stub-1",
                            "2026-04-30T12:00:00Z"
                    );
                });

        Map<String, Object> result = worker.handleJob(activatedJob);

        ArgumentCaptor<AsyncStubSubmissionRequest> requestCaptor =
                ArgumentCaptor.forClass(AsyncStubSubmissionRequest.class);
        verify(stubServiceClient).submitAsyncRequest(requestCaptor.capture());

        AsyncStubSubmissionRequest submittedRequest = requestCaptor.getValue();
        assertThat(submittedRequest.requestId()).startsWith("async-stub-");
        assertThat(submittedRequest.documentNumber()).isEqualTo("DOC-9001");
        assertThat(submittedRequest.customerId()).isEqualTo(42L);
        assertThat(submittedRequest.amount()).isEqualTo(850);
        assertThat(submittedRequest.processInstanceKey()).isEqualTo(200L);

        assertThat(result).containsEntry("correlationKey", submittedRequest.requestId());
        assertThat(result).containsEntry("asyncStubSubmissionStatus", "ACCEPTED");
        assertThat(result).containsEntry("asyncStubSubmissionMessage", "Stub service accepted request");
        assertThat(result).containsEntry("asyncStubTrackingId", "stub-1");
        assertThat(result).containsEntry("asyncStubAcceptedAt", "2026-04-30T12:00:00Z");
    }
}

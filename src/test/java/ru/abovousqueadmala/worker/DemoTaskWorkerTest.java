package ru.abovousqueadmala.worker;

import java.time.OffsetDateTime;
import java.util.Map;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DemoTaskWorkerTest {

    private final DemoTaskWorker demoTaskWorker = new DemoTaskWorker();

    @Test
    void handleJobReturnsCompletionVariables() {
        ActivatedJob activatedJob = mock(ActivatedJob.class);
        when(activatedJob.getKey()).thenReturn(100L);
        when(activatedJob.getProcessInstanceKey()).thenReturn(200L);
        when(activatedJob.getVariables()).thenReturn("{\"customerId\":42}");

        Map<String, Object> result = demoTaskWorker.handleJob(activatedJob);

        assertThat(result).containsEntry("workerStatus", "DONE");
        assertThat(result).containsEntry("approved", true);
        assertThat(OffsetDateTime.parse((String) result.get("processedAt"))).isNotNull();
    }
}

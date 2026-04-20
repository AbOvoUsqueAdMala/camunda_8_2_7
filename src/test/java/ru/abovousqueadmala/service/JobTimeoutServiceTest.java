package ru.abovousqueadmala.service;

import java.time.Duration;
import java.util.List;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.UpdateTimeoutJobCommandStep1;
import io.camunda.zeebe.client.api.response.UpdateTimeoutJobResponse;
import ru.abovousqueadmala.dto.BulkTimeoutUpdateResponse;
import ru.abovousqueadmala.dto.JobTimeoutUpdateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobTimeoutServiceTest {

    @Mock
    private ElasticJobService elasticJobService;

    @Mock
    private ZeebeClient zeebeClient;

    @Mock
    private UpdateTimeoutJobCommandStep1 updateTimeoutJobCommandStep1;

    @Mock
    private UpdateTimeoutJobCommandStep1.UpdateTimeoutJobCommandStep2 updateTimeoutJobCommandStep2;

    @Mock
    private ZeebeFuture<UpdateTimeoutJobResponse> zeebeFuture;

    private JobTimeoutService jobTimeoutService;

    @BeforeEach
    void setUp() {
        jobTimeoutService = new JobTimeoutService(elasticJobService, zeebeClient);
    }

    @Test
    void setTimeoutToOneSecondReturnsStatus() {
        when(zeebeClient.newUpdateTimeoutCommand(5L)).thenReturn(updateTimeoutJobCommandStep1);
        when(updateTimeoutJobCommandStep1.timeout(Duration.ofSeconds(1))).thenReturn(updateTimeoutJobCommandStep2);
        when(updateTimeoutJobCommandStep2.send()).thenReturn(zeebeFuture);
        when(zeebeFuture.join()).thenReturn(null);

        JobTimeoutUpdateResponse response = jobTimeoutService.setTimeoutToOneSecond(5L);

        assertThat(response).isEqualTo(new JobTimeoutUpdateResponse(5L, 1000L, "UPDATED"));
        verify(zeebeFuture).join();
    }

    @Test
    void shortenActiveTimeoutsContinuesWhenSingleUpdateFails() {
        when(elasticJobService.findLastActivatedJobKeys("demo-task", "demoTaskWorker#handleJob"))
                .thenReturn(List.of(10L, 20L, 30L));
        when(zeebeClient.newUpdateTimeoutCommand(anyLong())).thenReturn(updateTimeoutJobCommandStep1);
        when(updateTimeoutJobCommandStep1.timeout(Duration.ofSeconds(1))).thenReturn(updateTimeoutJobCommandStep2);
        when(updateTimeoutJobCommandStep2.send()).thenReturn(zeebeFuture);
        when(zeebeFuture.join())
                .thenReturn(null)
                .thenThrow(new RuntimeException("boom"))
                .thenReturn(null);

        BulkTimeoutUpdateResponse response = jobTimeoutService.shortenAllActiveDemoTaskTimeoutsToOneSecond();

        assertThat(response).isEqualTo(new BulkTimeoutUpdateResponse(3, 2, List.of(10L, 20L, 30L)));
        verify(zeebeClient, times(3)).newUpdateTimeoutCommand(anyLong());
    }
}

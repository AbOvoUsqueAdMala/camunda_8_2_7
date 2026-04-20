package ru.abovousqueadmala.service;

import java.time.Duration;
import java.util.List;
import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.abovousqueadmala.dto.BulkTimeoutUpdateResponse;
import ru.abovousqueadmala.dto.JobTimeoutUpdateResponse;
import org.springframework.stereotype.Service;

@Service
public class JobTimeoutService {

    private static final Logger log = LoggerFactory.getLogger(JobTimeoutService.class);

    static final String DEMO_TASK_TYPE = "demo-task";
    static final String DEMO_TASK_WORKER = "demoTaskWorker#handleJob";
    static final Duration SHORT_TIMEOUT = Duration.ofSeconds(1);
    static final String UPDATED_STATUS = "UPDATED";

    private final ElasticJobService elasticJobService;
    private final ZeebeClient zeebeClient;

    public JobTimeoutService(ElasticJobService elasticJobService, ZeebeClient zeebeClient) {
        this.elasticJobService = elasticJobService;
        this.zeebeClient = zeebeClient;
    }

    public JobTimeoutUpdateResponse setTimeoutToOneSecond(long jobKey) {
        zeebeClient.newUpdateTimeoutCommand(jobKey)
                .timeout(SHORT_TIMEOUT)
                .send()
                .join();

        return new JobTimeoutUpdateResponse(jobKey, SHORT_TIMEOUT.toMillis(), UPDATED_STATUS);
    }

    public BulkTimeoutUpdateResponse shortenAllActiveDemoTaskTimeoutsToOneSecond() {
        List<Long> keys = elasticJobService.findLastActivatedJobKeys(DEMO_TASK_TYPE, DEMO_TASK_WORKER);

        int updated = 0;

        for (Long key : keys) {
            try {
                zeebeClient.newUpdateTimeoutCommand(key)
                        .timeout(SHORT_TIMEOUT)
                        .send()
                        .join();
                updated++;
            } catch (Exception exception) {
                log.warn("Failed to update timeout for jobKey={}", key, exception);
            }
        }

        return new BulkTimeoutUpdateResponse(keys.size(), updated, List.copyOf(keys));
    }
}

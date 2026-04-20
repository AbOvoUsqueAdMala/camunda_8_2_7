package ru.abovousqueadmala;

import io.camunda.zeebe.client.ZeebeClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class JobTimeoutService {

    private final ElasticJobService elasticJobService;
    private final ZeebeClient zeebeClient;

    public JobTimeoutService(ElasticJobService elasticJobService, ZeebeClient zeebeClient) {
        this.elasticJobService = elasticJobService;
        this.zeebeClient = zeebeClient;
    }

    public Map<String, Object> shortenAllActiveDemoTaskTimeoutsToOneSecond() {
        List<Long> keys = elasticJobService.findLastActivatedJobKeys(
                "demo-task",
                "demoTaskWorker#handleJob"
        );

        int updated = 0;

        for (Long key : keys) {
            try {
                zeebeClient.newUpdateTimeoutCommand(key)
                        .timeout(Duration.ofSeconds(1))
                        .send()
                        .join();
                updated++;
            } catch (Exception e) {
                // можно залогировать и идти дальше
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("found", keys.size());
        result.put("updated", updated);
        result.put("keys", keys);
        return result;
    }
}
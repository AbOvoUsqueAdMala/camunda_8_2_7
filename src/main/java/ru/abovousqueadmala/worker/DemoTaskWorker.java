package ru.abovousqueadmala.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DemoTaskWorker {

    @JobWorker(type = "demo-task", timeout = 1000)
    public Map<String, Object> handleJob(final ActivatedJob job) {
        log.info(
                "Received job. key={}, processInstanceKey={}, variables={}",
                job.getKey(),
                job.getProcessInstanceKey(),
                job.getVariables()
        );

        Map<String, Object> resultVars = new HashMap<>();
        resultVars.put("workerStatus", "DONE");
        resultVars.put("processedAt", OffsetDateTime.now().toString());
        resultVars.put("approved", true);

        log.info("Job handled successfully. key={}", job.getKey());
        return resultVars;
    }
}

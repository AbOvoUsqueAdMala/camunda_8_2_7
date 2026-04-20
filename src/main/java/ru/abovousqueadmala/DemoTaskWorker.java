package ru.abovousqueadmala;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DemoTaskWorker {

    private static final Logger log = LoggerFactory.getLogger(DemoTaskWorker.class);

    @JobWorker(type = "demo-task", timeout = 10000000000L, autoComplete = false)
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

        log.info("Job completed. key={}", job.getKey());

//        throw new RuntimeException("123");

        return resultVars;
    }
}
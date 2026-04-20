package ru.abovousqueadmala;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessController {

    private final ZeebeClient zeebeClient;
    @Value("${app.camunda.zeebe.process-id}")
    private String processId;
    private final ElasticJobService elasticJobService;
    private final JobTimeoutService jobTimeoutService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startProcess(
            @RequestBody(required = false) StartProcessRequest request) {

        Map<String, Object> variables =
                request != null && request.getVariables() != null
                        ? request.getVariables()
                        : Collections.emptyMap();

        var event = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        Map<String, Object> response = new HashMap<>();
        response.put("processDefinitionKey", event.getProcessDefinitionKey());
        response.put("processInstanceKey", event.getProcessInstanceKey());
        response.put("version", event.getVersion());
        response.put("bpmnProcessId", event.getBpmnProcessId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{jobKey}/timeout/1s")
    public ResponseEntity<Map<String, Object>> setTimeoutToOneSecond(@PathVariable long jobKey) {
        zeebeClient.newUpdateTimeoutCommand(jobKey)
                .timeout(1000L)
                .send()
                .join();

        return ResponseEntity.ok(Map.of(
                "jobKey", jobKey,
                "timeoutMs", 1000,
                "status", "UPDATED"
        ));
    }

//    @PostMapping("/jobs/shorten-active-timeouts")
//    public ResponseEntity<?> shortenActiveTimeouts() {
//        List<Long> jobKeys = elasticJobService.findLastActivatedJobKeys("demo-task", null);
//
//        for (Long key : jobKeys) {
//            zeebeClient.newUpdateTimeoutCommand(key)
//                    .timeout(Duration.ofSeconds(1))
//                    .send()
//                    .join();
//        }
//
//        return ResponseEntity.ok(Map.of(
//                "updatedCount", jobKeys.size(),
//                "jobKeys", jobKeys
//        ));
//    }

    @PostMapping("/api/jobs/shorten-active-demo-task-timeouts")
    public ResponseEntity<Map<String, Object>> shortenActiveTimeouts() {
        return ResponseEntity.ok(
                jobTimeoutService.shortenAllActiveDemoTaskTimeoutsToOneSecond()
        );
    }

}


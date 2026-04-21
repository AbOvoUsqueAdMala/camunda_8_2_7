package ru.abovousqueadmala.controller;

import java.util.Collections;
import java.util.Map;
import ru.abovousqueadmala.dto.StartProcessRequest;
import ru.abovousqueadmala.dto.StartProcessResponse;
import ru.abovousqueadmala.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessInstanceController {

    private final ProcessService processService;

    public ProcessInstanceController(ProcessService processService) {
        this.processService = processService;
    }

    @PostMapping({"/api/process-instances", "/api/process/start"})
    public ResponseEntity<StartProcessResponse> startNewInstance(
            @RequestBody(required = false) StartProcessRequest request) {

        String processId = request != null ? request.processId() : null;
        Map<String, Object> variables = request != null && request.variables() != null
                ? request.variables()
                : Collections.emptyMap();

        return ResponseEntity.ok(processService.startProcess(processId, variables));
    }
}

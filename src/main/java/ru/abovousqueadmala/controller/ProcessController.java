package ru.abovousqueadmala.controller;

import java.util.Collections;
import java.util.Map;
import ru.abovousqueadmala.dto.StartProcessRequest;
import ru.abovousqueadmala.dto.StartProcessResponse;
import ru.abovousqueadmala.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @PostMapping("/start")
    public ResponseEntity<StartProcessResponse> startProcess(
            @RequestBody(required = false) StartProcessRequest request) {

        Map<String, Object> variables = request != null && request.variables() != null
                ? request.variables()
                : Collections.emptyMap();

        return ResponseEntity.ok(processService.startProcess(variables));
    }
}

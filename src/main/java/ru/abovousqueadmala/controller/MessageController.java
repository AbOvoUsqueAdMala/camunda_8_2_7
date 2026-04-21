package ru.abovousqueadmala.controller;

import java.util.Collections;
import java.util.Map;
import ru.abovousqueadmala.dto.ContinueProcessRequest;
import ru.abovousqueadmala.dto.ContinueProcessResponse;
import ru.abovousqueadmala.service.ProcessMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ProcessMessageService processMessageService;

    @PostMapping("/external-data")
    public ResponseEntity<ContinueProcessResponse> continueProcess(
            @RequestBody ContinueProcessRequest request) {

        Map<String, Object> variables = request.variables() != null
                ? request.variables()
                : Collections.emptyMap();

        return ResponseEntity.ok(processMessageService.publishExternalData(request.requestId(), variables));
    }
}

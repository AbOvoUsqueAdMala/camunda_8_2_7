package ru.abovousqueadmala.controller;

import java.time.OffsetDateTime;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.abovousqueadmala.dto.StubServiceRequest;
import ru.abovousqueadmala.dto.StubServiceResponse;

@RestController
@Slf4j
@RequestMapping("/api/stub/external-service")
@Tag(name = "Stub External Service", description = "Mock endpoint that simulates an external integration")
public class StubServiceController {

    @PostMapping
    @Operation(summary = "Accept data from the nested process and return a stub response")
    public ResponseEntity<StubServiceResponse> acceptPayload(@RequestBody StubServiceRequest request) {
        log.info(
                "Stub service received payload. requestId={}, documentNumber={}, processInstanceKey={}",
                request.requestId(),
                request.documentNumber(),
                request.processInstanceKey()
        );

        StubServiceResponse response = new StubServiceResponse(
                request.requestId(),
                "ACCEPTED",
                "Stub service processed request",
                "stub-" + UUID.randomUUID(),
                OffsetDateTime.now().toString()
        );

        return ResponseEntity.ok(response);
    }
}

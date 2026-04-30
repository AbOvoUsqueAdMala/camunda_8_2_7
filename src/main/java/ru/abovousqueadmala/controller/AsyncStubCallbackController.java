package ru.abovousqueadmala.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.abovousqueadmala.dto.AsyncStubCallbackRequest;
import ru.abovousqueadmala.dto.ContinueProcessResponse;
import ru.abovousqueadmala.service.AsyncStubCallbackService;

@RestController
@RequestMapping("/api/async-stub-callbacks")
@RequiredArgsConstructor
@Tag(name = "Async Stub Callbacks", description = "Operations for continuing async stub callback processes")
public class AsyncStubCallbackController {

    private final AsyncStubCallbackService asyncStubCallbackService;

    @PostMapping("/completed")
    @Operation(summary = "Continue async stub callback process with callback payload")
    public ResponseEntity<ContinueProcessResponse> completeAsyncRequest(
            @RequestBody AsyncStubCallbackRequest request) {

        if (request.requestId() == null || request.requestId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestId is required");
        }

        return ResponseEntity.ok(asyncStubCallbackService.publishCallback(request));
    }
}

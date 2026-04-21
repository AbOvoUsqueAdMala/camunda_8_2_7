package ru.abovousqueadmala.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.abovousqueadmala.dto.BulkTimeoutUpdateResponse;
import ru.abovousqueadmala.dto.JobTimeoutUpdateResponse;
import ru.abovousqueadmala.service.JobTimeoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Operations for active Camunda jobs")
public class JobController {

    private final JobTimeoutService jobTimeoutService;

    @PostMapping("/{jobKey}/timeout/1s")
    @Operation(summary = "Set job timeout to one second")
    public ResponseEntity<JobTimeoutUpdateResponse> setTimeoutToOneSecond(@PathVariable long jobKey) {
        return ResponseEntity.ok(jobTimeoutService.setTimeoutToOneSecond(jobKey));
    }

    @PostMapping("/shorten-active-demo-task-timeouts")
    @Operation(summary = "Shorten timeouts for all active demo-task jobs")
    public ResponseEntity<BulkTimeoutUpdateResponse> shortenActiveTimeouts() {
        return ResponseEntity.ok(jobTimeoutService.shortenAllActiveDemoTaskTimeoutsToOneSecond());
    }
}

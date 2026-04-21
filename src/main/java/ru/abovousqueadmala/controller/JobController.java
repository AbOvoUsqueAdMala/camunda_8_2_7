package ru.abovousqueadmala.controller;

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
public class JobController {

    private final JobTimeoutService jobTimeoutService;

    @PostMapping("/{jobKey}/timeout/1s")
    public ResponseEntity<JobTimeoutUpdateResponse> setTimeoutToOneSecond(@PathVariable long jobKey) {
        return ResponseEntity.ok(jobTimeoutService.setTimeoutToOneSecond(jobKey));
    }

    @PostMapping("/shorten-active-demo-task-timeouts")
    public ResponseEntity<BulkTimeoutUpdateResponse> shortenActiveTimeouts() {
        return ResponseEntity.ok(jobTimeoutService.shortenAllActiveDemoTaskTimeoutsToOneSecond());
    }
}

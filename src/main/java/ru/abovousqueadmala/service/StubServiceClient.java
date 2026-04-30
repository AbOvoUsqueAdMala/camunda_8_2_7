package ru.abovousqueadmala.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.abovousqueadmala.config.AppProperties;
import ru.abovousqueadmala.dto.AsyncStubSubmissionRequest;
import ru.abovousqueadmala.dto.StubServiceRequest;
import ru.abovousqueadmala.dto.StubServiceResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class StubServiceClient {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    public StubServiceResponse sendApprovalData(StubServiceRequest request) {
        return postToStubService(request, request.requestId());
    }

    public StubServiceResponse submitAsyncRequest(AsyncStubSubmissionRequest request) {
        return postToStubService(request, request.requestId());
    }

    private StubServiceResponse postToStubService(Object request, String requestId) {
        String url = UriComponentsBuilder.fromHttpUrl(appProperties.stubService().baseUrl())
                .path(appProperties.stubService().submitPath())
                .toUriString();

        log.info("Sending payload to stub service. requestId={}, url={}", requestId, url);

        ResponseEntity<StubServiceResponse> response = restTemplate.postForEntity(
                url,
                request,
                StubServiceResponse.class
        );

        StubServiceResponse body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Stub service returned an empty response body");
        }

        return body;
    }
}

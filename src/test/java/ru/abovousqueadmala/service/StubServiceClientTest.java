package ru.abovousqueadmala.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.abovousqueadmala.config.AppProperties;
import ru.abovousqueadmala.dto.AsyncStubSubmissionRequest;
import ru.abovousqueadmala.dto.StubServiceRequest;
import ru.abovousqueadmala.dto.StubServiceResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StubServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private StubServiceClient stubServiceClient;

    @BeforeEach
    void setUp() {
        stubServiceClient = new StubServiceClient(restTemplate, appProperties());
    }

    @Test
    void sendApprovalDataPostsPayloadToConfiguredUrl() {
        StubServiceRequest request = new StubServiceRequest("req-123", "DOC-77", "user-1", true, "DONE", 200L);
        StubServiceResponse response = new StubServiceResponse(
                "req-123",
                "ACCEPTED",
                "Stub service processed request",
                "stub-1",
                "2026-04-21T00:00:00Z"
        );

        when(restTemplate.postForEntity(
                eq("http://localhost:8080/api/stub/external-service"),
                eq(request),
                eq(StubServiceResponse.class)
        )).thenReturn(ResponseEntity.ok(response));

        StubServiceResponse result = stubServiceClient.sendApprovalData(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void submitAsyncRequestPostsPayloadToConfiguredUrl() {
        AsyncStubSubmissionRequest request = new AsyncStubSubmissionRequest("async-req-123", "DOC-9001", 42L, 850, 200L);
        StubServiceResponse response = new StubServiceResponse(
                "async-req-123",
                "ACCEPTED",
                "Stub service accepted request",
                "stub-1",
                "2026-04-30T12:00:00Z"
        );

        when(restTemplate.postForEntity(
                eq("http://localhost:8080/api/stub/external-service/async"),
                eq(request),
                eq(StubServiceResponse.class)
        )).thenReturn(ResponseEntity.ok(response));

        StubServiceResponse result = stubServiceClient.submitAsyncRequest(request);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void sendApprovalDataFailsWhenStubResponseBodyIsMissing() {
        StubServiceRequest request = new StubServiceRequest("req-123", null, null, true, "DONE", 200L);

        when(restTemplate.postForEntity(
                eq("http://localhost:8080/api/stub/external-service"),
                eq(request),
                eq(StubServiceResponse.class)
        )).thenReturn(ResponseEntity.ok(null));

        assertThatThrownBy(() -> stubServiceClient.sendApprovalData(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Stub service returned an empty response body");
    }

    private static AppProperties appProperties() {
        return new AppProperties(
                new AppProperties.Camunda(
                        "jobs-*",
                        new AppProperties.Zeebe("demo-process")
                ),
                new AppProperties.Elastic("http://localhost:9200"),
                new AppProperties.StubService(
                        "http://localhost:8080",
                        "/api/stub/external-service",
                        "/api/stub/external-service/async"
                )
        );
    }
}

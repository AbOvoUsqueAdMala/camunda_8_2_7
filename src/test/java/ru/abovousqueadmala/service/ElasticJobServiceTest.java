package ru.abovousqueadmala.service;

import java.util.List;
import java.util.Map;
import ru.abovousqueadmala.dto.ActiveJobInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticJobServiceTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private ElasticJobService elasticJobService;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        elasticJobService = new ElasticJobService(restTemplateBuilder, "http://localhost:9200", "jobs-*");
    }

    @Test
    void findLastActivatedJobKeysReturnsOnlyActivatedJobs() {
        ElasticJobService.ElasticSearchResponse response = responseWithHits(
                hit(10L, "ACTIVATED", "demo-task", "demoTaskWorker#handleJob"),
                hit(20L, "COMPLETED", "demo-task", "demoTaskWorker#handleJob")
        );

        when(restTemplate.exchange(
                eq("http://localhost:9200/jobs-*/_search"),
                eq(HttpMethod.POST),
                org.mockito.ArgumentMatchers.<HttpEntity<Map<String, Object>>>any(),
                eq(ElasticJobService.ElasticSearchResponse.class)
        )).thenReturn(ResponseEntity.ok(response));

        List<Long> result = elasticJobService.findLastActivatedJobKeys("demo-task", null);

        assertThat(result).containsExactly(10L);
    }

    @Test
    void findLastActivatedJobsAddsWorkerFilterAndMapsPayload() {
        ElasticJobService.ElasticSearchResponse response = responseWithHits(
                hit(10L, "ACTIVATED", "demo-task", "demoTaskWorker#handleJob")
        );

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.exchange(
                eq("http://localhost:9200/jobs-*/_search"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(ElasticJobService.ElasticSearchResponse.class)
        )).thenReturn(ResponseEntity.ok(response));

        List<ActiveJobInfo> result = elasticJobService.findLastActivatedJobs("demo-task", "demoTaskWorker#handleJob");

        assertThat(result).containsExactly(new ActiveJobInfo(
                10L,
                "ACTIVATED",
                "2026-04-21T00:00:00Z",
                123L,
                "demo-task",
                "demoTaskWorker#handleJob",
                1000L,
                2000L,
                3000L,
                "service_task_1"
        ));

        Map<String, Object> body = entityCaptor.getValue().getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsEntry("size", 1000);
        assertThat(body).containsKey("collapse");

        Map<String, Object> query = cast(body.get("query"));
        Map<String, Object> bool = cast(query.get("bool"));
        List<Map<String, Object>> filters = cast(bool.get("filter"));

        assertThat(filters).containsExactly(
                Map.of("term", Map.of("value.type", "demo-task")),
                Map.of("term", Map.of("value.worker", "demoTaskWorker#handleJob"))
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value) {
        return (T) value;
    }

    private static ElasticJobService.ElasticSearchResponse responseWithHits(ElasticJobService.Hit... hits) {
        ElasticJobService.ElasticSearchResponse response = new ElasticJobService.ElasticSearchResponse();
        response.hits = new ElasticJobService.Hits();
        response.hits.hits = List.of(hits);
        return response;
    }

    private static ElasticJobService.Hit hit(long key, String intent, String type, String worker) {
        ElasticJobService.ValueInherited value = new ElasticJobService.ValueInherited();
        value.type = type;
        value.worker = worker;
        value.timeout = 1000L;
        value.deadline = 2000L;
        value.processInstanceKey = 3000L;
        value.elementId = "service_task_1";

        ElasticJobService.Source source = new ElasticJobService.Source();
        source.key = key;
        source.intent = intent;
        source.timestamp = "2026-04-21T00:00:00Z";
        source.position = 123L;
        source.value = value;

        ElasticJobService.Hit hit = new ElasticJobService.Hit();
        hit.source = source;
        return hit;
    }
}

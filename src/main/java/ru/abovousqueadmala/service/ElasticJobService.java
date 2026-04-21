package ru.abovousqueadmala.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.abovousqueadmala.config.AppProperties;
import ru.abovousqueadmala.dto.ActiveJobInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ElasticJobService {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    public List<Long> findLastActivatedJobKeys(String jobType, String workerName) {
        ElasticSearchResponse response = searchCollapsedLatestJobs(jobType, workerName);

        List<Long> result = new ArrayList<>();
        if (response == null || response.hits == null || response.hits.hits == null) {
            return result;
        }

        for (Hit hit : response.hits.hits) {
            if (hit == null || hit.source == null) {
                continue;
            }

            if ("ACTIVATED".equals(hit.source.intent) && hit.source.key != null) {
                result.add(hit.source.key);
            }
        }

        return result;
    }

    public List<ActiveJobInfo> findLastActivatedJobs(String jobType, String workerName) {
        ElasticSearchResponse response = searchCollapsedLatestJobs(jobType, workerName);

        List<ActiveJobInfo> result = new ArrayList<>();
        if (response == null || response.hits == null || response.hits.hits == null) {
            return result;
        }

        for (Hit hit : response.hits.hits) {
            if (hit == null || hit.source == null) {
                continue;
            }

            Source source = hit.source;
            if (!"ACTIVATED".equals(source.intent) || source.key == null) {
                continue;
            }

            result.add(new ActiveJobInfo(
                    source.key,
                    source.intent,
                    source.timestamp,
                    source.position,
                    source.value != null ? source.value.type : null,
                    source.value != null ? source.value.worker : null,
                    source.value != null ? source.value.timeout : null,
                    source.value != null ? source.value.deadline : null,
                    source.value != null ? source.value.processInstanceKey : null,
                    source.value != null ? source.value.elementId : null
            ));
        }

        return result;
    }

    private ElasticSearchResponse searchCollapsedLatestJobs(String jobType, String workerName) {
        String elasticUrl = appProperties.elastic().url();
        String jobIndexPattern = appProperties.camunda().jobIndex();
        String url = elasticUrl + "/" + jobIndexPattern + "/_search";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", 1000);
        body.put("sort", List.of(Map.of("position", Map.of("order", "desc"))));
        body.put("collapse", Map.of("field", "key"));
        body.put("_source", List.of(
                "key",
                "intent",
                "timestamp",
                "position",
                "value.type",
                "value.worker",
                "value.timeout",
                "value.deadline",
                "value.processInstanceKey",
                "value.elementId"
        ));

        List<Map<String, Object>> filters = new ArrayList<>();
        filters.add(Map.of("term", Map.of("value.type", jobType)));

        if (workerName != null && !workerName.isBlank()) {
            filters.add(Map.of("term", Map.of("value.worker", workerName)));
        }

        body.put("query", Map.of("bool", Map.of("filter", filters)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<ElasticSearchResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                ElasticSearchResponse.class
        );

        return response.getBody();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class ElasticSearchResponse {
        Hits hits;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class Hits {
        List<Hit> hits;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class Hit {
        @JsonProperty("_source")
        Source source;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class Source {
        Long key;
        String intent;
        String timestamp;
        Long position;
        ValueInherited value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class ValueInherited {
        String type;
        String worker;
        Long timeout;
        Long deadline;
        Long processInstanceKey;
        String elementId;
    }
}

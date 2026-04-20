package ru.abovousqueadmala;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ElasticJobService {

    private final RestTemplate restTemplate;
    private final String elasticUrl;
    private final String jobIndexPattern;

    public ElasticJobService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${app.elastic.url}") String elasticUrl,
            @Value("${app.camunda.job-index}") String jobIndexPattern
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.elasticUrl = elasticUrl;
        this.jobIndexPattern = jobIndexPattern;
    }

    /**
     * Найти key тех job, у которых последнее событие = ACTIVATED.
     *
     * @param jobType тип job, например "demo-task"
     * @param workerName имя worker, например "demoTaskWorker#handleJob"; можно null
     */
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

            String intent = hit.source.intent;
            Long key = hit.source.key;

            if ("ACTIVATED".equals(intent) && key != null) {
                result.add(key);
            }
        }

        return result;
    }

    /**
     * Вернуть полную информацию по job, у которых последнее событие = ACTIVATED.
     */
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

            Source src = hit.source;
            if (!"ACTIVATED".equals(src.intent) || src.key == null) {
                continue;
            }

            ActiveJobInfo info = new ActiveJobInfo();
            info.setKey(src.key);
            info.setIntent(src.intent);
            info.setTimestamp(src.timestamp);
            info.setPosition(src.position);

            if (src.value != null) {
                info.setType(src.value.type);
                info.setWorker(src.value.worker);
                info.setTimeout(src.value.timeout);
                info.setDeadline(src.value.deadline);
                info.setProcessInstanceKey(src.value.processInstanceKey);
                info.setElementId(src.value.elementId);
            }

            result.add(info);
        }

        return result;
    }

    private ElasticSearchResponse searchCollapsedLatestJobs(String jobType, String workerName) {
        String url = elasticUrl + "/" + jobIndexPattern + "/_search";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", 1000);

        List<Map<String, Object>> sort = new ArrayList<>();
        sort.add(Map.of("position", Map.of("order", "desc")));
        body.put("sort", sort);

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

        body.put("query", Map.of(
                "bool", Map.of(
                        "filter", filters
                )
        ));

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
    static class ElasticSearchResponse {
        public Hits hits;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Hits {
        public List<Hit> hits;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Hit {
        @JsonProperty("_source")
        public Source source;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Source {
        public Long key;
        public String intent;
        public String timestamp;
        public Long position;
        public ValueInherited value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ValueInherited {
        public String type;
        public String worker;
        public Long timeout;
        public Long deadline;
        public Long processInstanceKey;
        public String elementId;
    }
}
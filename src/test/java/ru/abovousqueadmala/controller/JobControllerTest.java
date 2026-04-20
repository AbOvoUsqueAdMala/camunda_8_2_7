package ru.abovousqueadmala.controller;

import java.util.List;
import ru.abovousqueadmala.dto.BulkTimeoutUpdateResponse;
import ru.abovousqueadmala.dto.JobTimeoutUpdateResponse;
import ru.abovousqueadmala.service.JobTimeoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobTimeoutService jobTimeoutService;

    @Test
    void setTimeoutToOneSecondReturnsUpdatedStatus() throws Exception {
        given(jobTimeoutService.setTimeoutToOneSecond(77L))
                .willReturn(new JobTimeoutUpdateResponse(77L, 1000L, "UPDATED"));

        mockMvc.perform(post("/api/jobs/77/timeout/1s"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobKey").value(77))
                .andExpect(jsonPath("$.timeoutMs").value(1000))
                .andExpect(jsonPath("$.status").value("UPDATED"));
    }

    @Test
    void shortenActiveTimeoutsReturnsBulkResult() throws Exception {
        given(jobTimeoutService.shortenAllActiveDemoTaskTimeoutsToOneSecond())
                .willReturn(new BulkTimeoutUpdateResponse(3, 2, List.of(10L, 20L, 30L)));

        mockMvc.perform(post("/api/jobs/shorten-active-demo-task-timeouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(3))
                .andExpect(jsonPath("$.updated").value(2))
                .andExpect(jsonPath("$.keys[0]").value(10))
                .andExpect(jsonPath("$.keys[2]").value(30));
    }
}

package ru.abovousqueadmala.service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.abovousqueadmala.dto.AsyncStubCallbackRequest;
import ru.abovousqueadmala.dto.ContinueProcessResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncStubCallbackServiceTest {

    @Mock
    private ZeebeClient zeebeClient;

    @Mock
    private PublishMessageCommandStep1 publishMessageCommandStep1;

    @Mock
    private PublishMessageCommandStep1.PublishMessageCommandStep2 publishMessageCommandStep2;

    @Mock
    private PublishMessageCommandStep1.PublishMessageCommandStep3 publishMessageCommandStep3;

    @Mock
    private ZeebeFuture<PublishMessageResponse> zeebeFuture;

    @Mock
    private PublishMessageResponse publishMessageResponse;

    private AsyncStubCallbackService asyncStubCallbackService;

    @BeforeEach
    void setUp() {
        asyncStubCallbackService = new AsyncStubCallbackService(zeebeClient);
    }

    @Test
    void publishCallbackPublishesConfiguredMessageWithCallbackPayload() {
        AsyncStubCallbackRequest request = new AsyncStubCallbackRequest(
                "async-req-123",
                "COMPLETED",
                "External processing finished",
                "stub-1",
                Map.of("callbackResult", "OK")
        );
        Map<String, Object> expectedPayload = Map.of(
                "callbackResult", "OK",
                "asyncStubCallbackStatus", "COMPLETED",
                "asyncStubCallbackMessage", "External processing finished",
                "asyncStubCallbackTrackingId", "stub-1"
        );

        when(zeebeClient.newPublishMessageCommand()).thenReturn(publishMessageCommandStep1);
        when(publishMessageCommandStep1.messageName("async-stub-callback-received")).thenReturn(publishMessageCommandStep2);
        when(publishMessageCommandStep2.correlationKey("async-req-123")).thenReturn(publishMessageCommandStep3);
        when(publishMessageCommandStep3.variables(expectedPayload)).thenReturn(publishMessageCommandStep3);
        when(publishMessageCommandStep3.send()).thenReturn(zeebeFuture);
        when(zeebeFuture.join()).thenReturn(publishMessageResponse);
        when(publishMessageResponse.getMessageKey()).thenReturn(99L);

        ContinueProcessResponse response = asyncStubCallbackService.publishCallback(request);

        assertThat(response).isEqualTo(new ContinueProcessResponse(
                99L,
                "async-stub-callback-received",
                "async-req-123",
                "PUBLISHED"
        ));
        verify(publishMessageCommandStep3).variables(expectedPayload);
    }

    @Test
    void publishCallbackRejectsBlankRequestId() {
        AsyncStubCallbackRequest request = new AsyncStubCallbackRequest(
                " ",
                "COMPLETED",
                null,
                null,
                null
        );

        assertThatThrownBy(() -> asyncStubCallbackService.publishCallback(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("requestId is required");
    }
}

package ru.abovousqueadmala.service;

import java.util.Collections;
import java.util.Map;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import ru.abovousqueadmala.dto.ContinueProcessResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessMessageServiceTest {

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

    private ProcessMessageService processMessageService;

    @BeforeEach
    void setUp() {
        processMessageService = new ProcessMessageService(zeebeClient);
    }

    @Test
    void publishExternalDataPublishesMessageWithVariables() {
        Map<String, Object> variables = Map.of("documentNumber", "DOC-77");

        when(zeebeClient.newPublishMessageCommand()).thenReturn(publishMessageCommandStep1);
        when(publishMessageCommandStep1.messageName("external-data-received")).thenReturn(publishMessageCommandStep2);
        when(publishMessageCommandStep2.correlationKey("req-123")).thenReturn(publishMessageCommandStep3);
        when(publishMessageCommandStep3.variables(variables)).thenReturn(publishMessageCommandStep3);
        when(publishMessageCommandStep3.send()).thenReturn(zeebeFuture);
        when(zeebeFuture.join()).thenReturn(publishMessageResponse);
        when(publishMessageResponse.getMessageKey()).thenReturn(77L);

        ContinueProcessResponse response = processMessageService.publishExternalData("req-123", variables);

        assertThat(response).isEqualTo(new ContinueProcessResponse(
                77L,
                "external-data-received",
                "req-123",
                "PUBLISHED"
        ));
        verify(publishMessageCommandStep3).variables(variables);
        verify(zeebeFuture).join();
    }

    @Test
    void publishExternalDataUsesEmptyPayloadWhenVariablesAreMissing() {
        when(zeebeClient.newPublishMessageCommand()).thenReturn(publishMessageCommandStep1);
        when(publishMessageCommandStep1.messageName("external-data-received")).thenReturn(publishMessageCommandStep2);
        when(publishMessageCommandStep2.correlationKey("req-123")).thenReturn(publishMessageCommandStep3);
        when(publishMessageCommandStep3.variables(Collections.emptyMap())).thenReturn(publishMessageCommandStep3);
        when(publishMessageCommandStep3.send()).thenReturn(zeebeFuture);
        when(zeebeFuture.join()).thenReturn(publishMessageResponse);
        when(publishMessageResponse.getMessageKey()).thenReturn(88L);

        ContinueProcessResponse response = processMessageService.publishExternalData("req-123", null);

        assertThat(response.messageKey()).isEqualTo(88L);
        verify(publishMessageCommandStep3).variables(Collections.emptyMap());
    }
}

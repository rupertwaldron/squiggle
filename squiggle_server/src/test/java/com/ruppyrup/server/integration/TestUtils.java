package com.ruppyrup.server.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.server.model.DrawPoint;

import static com.ruppyrup.server.integration.config.LoggingExtension.listAppender;
import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static DrawPoint getMessage(String receivedMessage) throws JsonProcessingException {
        return mapper.readValue(receivedMessage, DrawPoint.class);
    }

    public static void assertLogMessage(String logMessage) {
        listAppender.list.stream()
                .filter(log -> log.getFormattedMessage().contains(logMessage))
                .findFirst()
                .ifPresent(log -> assertThat(log.getFormattedMessage())
                        .containsSubsequence(logMessage));
    }
}

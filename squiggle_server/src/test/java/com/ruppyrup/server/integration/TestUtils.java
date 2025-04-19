package com.ruppyrup.server.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.server.model.DrawPoint;

public class TestUtils {

    public static final ObjectMapper mapper = new ObjectMapper();

    public static DrawPoint getMessage(String receivedMessage) throws JsonProcessingException {
        return mapper.readValue(receivedMessage, DrawPoint.class);
    }
}

package com.ruppyrup.client.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record DrawPoint(int x, int y, String lineWidth, String strokeStyle, boolean isFilled) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public String toJson() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }

    public static DrawPoint fromJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, DrawPoint.class);
    }
}

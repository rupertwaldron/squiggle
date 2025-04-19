package com.ruppyrup.server.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface Jsonisable {

    ObjectMapper mapper = new ObjectMapper();

    default String toJson() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }

    static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return mapper.readValue(json, clazz);
    }
}

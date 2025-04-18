package com.ruppyrup.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DrawPoint(String action,
                        String playerId,
                        Integer x,
                        Integer y,
                        String lineWidth,
                        String strokeStyle,
                        Boolean isFilled,
                        String guessWord,
                        String gameId
) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public String toJson() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }

    public static DrawPoint fromJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, DrawPoint.class);
    }
}

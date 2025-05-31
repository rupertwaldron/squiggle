package com.ruppyrup.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;


@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DrawPoint(String action,
                        String playerId,
                        Integer x,
                        Integer y,
                        String lineWidth,
                        String strokeStyle,
                        Boolean isFilled,
                        String guessWord,
                        String gameId,
                        List<String> players
) implements Jsonisable { }

package com.ruppyrup.server.model;

import org.springframework.web.socket.WebSocketSession;


public record Player(String playerId, WebSocketSession session) {
}

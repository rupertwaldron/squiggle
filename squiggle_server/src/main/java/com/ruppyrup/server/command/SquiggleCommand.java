package com.ruppyrup.server.command;

import com.ruppyrup.server.model.DrawPoint;
import org.springframework.web.socket.WebSocketSession;

public interface SquiggleCommand {
    void execute(WebSocketSession session, DrawPoint drawPoint);
}

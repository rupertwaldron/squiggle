package com.ruppyrup.server.command;

import com.ruppyrup.server.model.DrawPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class NullCommand implements SquiggleCommand {
    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        log.warn("Null commamd triggered {} on thread {}", drawPoint, Thread.currentThread());
    }
}

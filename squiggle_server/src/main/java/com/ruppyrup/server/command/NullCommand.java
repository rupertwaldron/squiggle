package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class NullCommand implements SquiggleCommand {
    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        log.warn("Null commamd triggered {} on thread {}", drawPoint, Thread.currentThread());
    }
}

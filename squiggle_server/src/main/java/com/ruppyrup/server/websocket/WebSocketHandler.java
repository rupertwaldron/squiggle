package com.ruppyrup.server.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.command.SquiggleCommandFactory;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Jsonisable;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {
    private final MessageService messageService;
    private final SquiggleCommandFactory squiggleCommandFactory;

    public WebSocketHandler(MessageService messageService, SquiggleCommandFactory squiggleCommandFactory) {
        this.messageService = messageService;
        this.squiggleCommandFactory = squiggleCommandFactory;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Session established with principle {} at {}", session.getPrincipal(), session.getRemoteAddress());
//        messageService.addSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
//        messageService.removeSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        log.info("Received message {} on thread {}", message.getPayload(), Thread.currentThread());

        DrawPoint drawPoint = Jsonisable.fromJson(message.getPayload(), DrawPoint.class);

        squiggleCommandFactory.getCommand(drawPoint.action()).execute(session, drawPoint);
    }
}

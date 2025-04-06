package com.ruppyrup.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

@Slf4j
public class MessageService {
    private final ExecutorService executor;
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    public MessageService(ExecutorService executor) {
        this.executor = executor;
    }

    public void sendInfo(WebSocketSession receivingSession, String info) {
        log.info("Send info called by thread {} with info => {}", Thread.currentThread().getName(), info);
        if (sessions.isEmpty()) return;

        for (WebSocketSession session : sessions) {
            if (session == receivingSession && !info.contains("winner") && !info.contains("reveal")) continue;
            // Don't send to the session that sent the message
            executor.submit(() -> {
                safeSend(session, info);
                log.info("Sent message {} on thread {}", info, Thread.currentThread());
            });
        }
    }

    private void safeSend(WebSocketSession session, String info) {
        try {
            session.sendMessage(new TextMessage(info));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }
}

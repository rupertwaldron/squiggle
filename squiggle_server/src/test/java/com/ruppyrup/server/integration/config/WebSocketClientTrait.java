package com.ruppyrup.server.integration.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface WebSocketClientTrait {

    List<WebsocketClientEndpoint> clientEndPoints = new ArrayList<>();
    Queue<String> recievedMessages = new ConcurrentLinkedQueue<>();


    default void connectWebsocketClient(int port) {
        try {
            // open websocket
            WebsocketClientEndpoint clientEndpoint = new WebsocketClientEndpoint(new URI("ws://localhost:" +
                    port + "/websocket"));
            clientEndPoints.add(clientEndpoint);
            // add listener
            clientEndpoint.addMessageHandler(message -> {
                recievedMessages.add(message);
                System.out.println("Message received = " + message + " from thread " + Thread.currentThread().getName() + "size = " + recievedMessages.size());
            });
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
    }
}

package com.ruppyrup.server.integration.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public interface WebSocketClientTrait {

    List<WebsocketClientEndpoint> clientEndPoints = new ArrayList<>();
    List<String> recievedMessages = new ArrayList<>();


    default void connectWebsocketClient(int port) {
        try {
            // open websocket
            WebsocketClientEndpoint clientEndpoint = new WebsocketClientEndpoint(new URI("ws://localhost:" +
                    port + "/websocket"));
            clientEndPoints.add(clientEndpoint);
            // add listener
            clientEndpoint.addMessageHandler(message -> {
                System.out.println("Message received = " + message);
                recievedMessages.add(message);
            });
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
    }
}

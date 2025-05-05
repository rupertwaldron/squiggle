package com.ruppyrup.server.integration.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.repository.GameRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.ruppyrup.server.integration.TestUtils.mapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public interface WebSocketClientTrait {

    String PLAYER_2 = "Player2";
    String PLAYER_1 = "Player1";
    String PLAYER_3 = "Player3";
    String PLAYER_4 = "Player4";

    String GAME_1 = "game1";
    String GAME_2 = "game2";

    List<WebsocketClientEndpoint> clientEndPoints = new CopyOnWriteArrayList<>();
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
                System.out.println("Message received = " + message + " from thread " + Thread.currentThread().getName() + "size = " + recievedMessages.size() + " with clients " + clientEndPoints.size());
            });
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
    }

    default void twoPlayersEnterTheSameRoom(String gameId, int port, String player1, String player2, GameRepository gameRepository) throws JsonProcessingException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        Game game = new Game(gameId);

        gameRepository.addGame(game);

        DrawPoint drawPoint1 = DrawPoint.builder()
                .action("enterRoom")
                .playerId(player1)
                .gameId(gameId)
                .build();

        String message1 = mapper.writeValueAsString(drawPoint1);
        clientEndPoints.getFirst().sendMessage(message1);

        DrawPoint drawPoint2 = DrawPoint.builder()
                .action("enterRoom")
                .playerId(player2)
                .gameId(gameId)
                .build();

        String message2 = mapper.writeValueAsString(drawPoint2);
        clientEndPoints.getLast().sendMessage(message2);

        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> !recievedMessages.isEmpty());

        assertThat(recievedMessages.size()).isEqualTo(2);
        recievedMessages.remove();
        recievedMessages.remove();
    }
}

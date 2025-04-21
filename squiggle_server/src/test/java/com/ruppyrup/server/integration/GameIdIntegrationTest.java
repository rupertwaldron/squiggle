package com.ruppyrup.server.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.integration.config.LoggingExtension;
import com.ruppyrup.server.integration.config.WebSocketClientTrait;
import com.ruppyrup.server.integration.config.WebsocketClientEndpoint;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.repository.WordRepository;
import jakarta.websocket.CloseReason;
import lombok.SneakyThrows;
import org.awaitility.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.ruppyrup.server.integration.TestUtils.getMessage;
import static com.ruppyrup.server.integration.TestUtils.mapper;
import static jakarta.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"reveal.count=2"})
@ExtendWith(LoggingExtension.class)
public class GameIdIntegrationTest implements WebSocketClientTrait {
    private static final String PLAYER_2 = "Player2";
    private static final String PLAYER_1 = "Player1";

    private static final String GAME_1 = "game1";
    private static final String GAME_2 = "game2";

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private GameRepository gameRepository;

    @Value("${reveal.count}")
    private int revealCount;


    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        wordRepository.reset();
        gameRepository.clearGames();
    }

    @AfterEach
    void closeConnection() {
        clientEndPoints.forEach(this::closeSession);
        clientEndPoints.clear();
        recievedMessages.clear();
    }

    @SneakyThrows
    private void closeSession(WebsocketClientEndpoint clientEndPoint) {
        clientEndPoint.getUserSession().close(new CloseReason(NORMAL_CLOSURE, "Finished test"));
    }

    @Test
    void playerReceivesMessagesFromSameGame() throws JsonProcessingException {
        twoPlayersEnterTheSameRoom();

        DrawPoint drawPoint = DrawPoint.builder()
                .action("mousemove")
                .playerId(PLAYER_1)
                .x(1000)
                .y(0)
                .lineWidth("5")
                .strokeStyle("red")
                .isFilled(false)
                .gameId(GAME_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);
        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !recievedMessages.isEmpty());

        DrawPoint received = getMessage(recievedMessages.poll());

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(drawPoint);
    }

    @Test
    void playerDoesNotReceiveMessagesFromDifferentGame() throws JsonProcessingException {
        twoPlayersEnterDifferentRooms();

        DrawPoint drawPoint = DrawPoint.builder()
                .action("mousemove")
                .playerId(PLAYER_1)
                .x(1000)
                .y(0)
                .lineWidth("5")
                .strokeStyle("red")
                .isFilled(false)
                .build();

        String message = mapper.writeValueAsString(drawPoint);
        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .pollDelay(Duration.FIVE_SECONDS)
                .pollInterval(Duration.ONE_SECOND)
                .until(recievedMessages::isEmpty);
    }

    private void twoPlayersEnterTheSameRoom() throws JsonProcessingException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        Game game = Game.builder()
                .gameId(GAME_1)
                .build();

        gameRepository.addGame(game);

        DrawPoint drawPoint1 = DrawPoint.builder()
                .action("enterRoom")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();

        String message1 = mapper.writeValueAsString(drawPoint1);
        clientEndPoints.getFirst().sendMessage(message1);

        DrawPoint drawPoint2 = DrawPoint.builder()
                .action("enterRoom")
                .playerId(PLAYER_2)
                .gameId(GAME_1)
                .build();

        String message2 = mapper.writeValueAsString(drawPoint2);
        clientEndPoints.getLast().sendMessage(message2);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !recievedMessages.isEmpty());

        assertThat(recievedMessages.size()).isEqualTo(2);
        recievedMessages.remove();
        recievedMessages.remove();
    }

    private void twoPlayersEnterDifferentRooms() throws JsonProcessingException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        Game game1 = Game.builder()
                .gameId(GAME_1)
                .build();

        Game game2 = Game.builder()
                .gameId(GAME_2)
                .build();

        gameRepository.addGame(game1);
        gameRepository.addGame(game2);

        DrawPoint drawPoint1 = DrawPoint.builder()
                .action("enterRoom")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();

        String message1 = mapper.writeValueAsString(drawPoint1);
        clientEndPoints.getFirst().sendMessage(message1);

        DrawPoint drawPoint2 = DrawPoint.builder()
                .action("enterRoom")
                .playerId(PLAYER_2)
                .gameId(GAME_2)
                .build();

        String message2 = mapper.writeValueAsString(drawPoint2);
        clientEndPoints.getLast().sendMessage(message2);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !recievedMessages.isEmpty());

        assertThat(recievedMessages.size()).isEqualTo(2);
        recievedMessages.remove();
        recievedMessages.remove();
    }
}

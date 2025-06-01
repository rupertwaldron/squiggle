package com.ruppyrup.server.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.integration.config.LoggingExtension;
import com.ruppyrup.server.integration.config.WebSocketClientTrait;
import com.ruppyrup.server.integration.config.WebsocketClientEndpoint;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.repository.WordRepository;
import jakarta.websocket.CloseReason;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;

import static com.ruppyrup.server.integration.TestUtils.getMessage;
import static com.ruppyrup.server.integration.TestUtils.mapper;
import static com.ruppyrup.server.integration.config.LoggingExtension.listAppender;
import static jakarta.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"reveal.count=2"})
@ExtendWith(LoggingExtension.class)
public class PlayerListIntegrationTest implements WebSocketClientTrait {
    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private GameRepository gameRepository;

    @Value("${reveal.count}")
    private int revealCount;


    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        wordRepository.reset();
        gameRepository.clearGames();
        twoPlayersEnterTheSameRoom(GAME_1, port, PLAYER_1, PLAYER_2, gameRepository);
    }

    @AfterEach
    void closeConnection() {
        clientEndPoints.values().forEach(this::closeSession);
        clientEndPoints.clear();
        recievedMessages.clear();
        listAppender.list.clear();
    }

    @SneakyThrows
    private void closeSession(WebsocketClientEndpoint clientEndPoint) {
        clientEndPoint.getUserSession().close(new CloseReason(NORMAL_CLOSURE, "Finished test"));
    }

    @Test
    void allPlayersReceivePlayerListWhenNewPlayerEnters() throws JsonProcessingException {
        connectWebsocketClient(port, PLAYER_3);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("enterRoom")
                .playerId(PLAYER_3)
                .gameId(GAME_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);
        clientEndPoints.get(PLAYER_3).sendMessage(message);

        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> recievedMessages.size() == 3);

        DrawPoint expecteddrawPoint = DrawPoint.builder()
                .action("enterRoom")
                .playerId(PLAYER_3)
                .gameId(GAME_1)
                .players(List.of(PLAYER_1, PLAYER_2, PLAYER_3))
                .build();

        DrawPoint received = getMessage(recievedMessages.poll());

        assertThat(received)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expecteddrawPoint);


        assertThat(gameRepository.getGameById(GAME_1).getPlayers())
                .extracting("playerId")
                .containsExactlyInAnyOrder(PLAYER_1, PLAYER_2, PLAYER_3);
    }

    @Test
    void allPlayersReceivePlayerListWhenPlayerLeavesGame() throws JsonProcessingException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("exitRoom")
                .playerId(PLAYER_2)
                .gameId(GAME_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);
        clientEndPoints.get(PLAYER_2).sendMessage(message);

        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> recievedMessages.size() == 1);

        DrawPoint expecteddrawPoint = DrawPoint.builder()
                .action("exitRoom")
                .playerId(PLAYER_2)
                .gameId(GAME_1)
                .players(List.of(PLAYER_1))
                .build();

        DrawPoint received = getMessage(recievedMessages.poll());

        assertThat(received)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expecteddrawPoint);


        assertThat(gameRepository.getGameById(GAME_1).getPlayers())
                .extracting("playerId")
                .containsExactlyInAnyOrder(PLAYER_1);
    }
}

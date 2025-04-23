package com.ruppyrup.server.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.integration.config.LoggingExtension;
import com.ruppyrup.server.integration.config.LoggingExtensionConfig;
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
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;

import static com.ruppyrup.server.integration.TestUtils.getMessage;
import static com.ruppyrup.server.integration.TestUtils.mapper;
import static com.ruppyrup.server.integration.config.LoggingExtension.listAppender;
import static jakarta.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"reveal.count=2"})
@ExtendWith(LoggingExtension.class)
public class GameLogisticsIntegrationTest implements WebSocketClientTrait {
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
        clientEndPoints.forEach(this::closeSession);
        clientEndPoints.clear();
        recievedMessages.clear();
        listAppender.list.clear();
    }

    @SneakyThrows
    private void closeSession(WebsocketClientEndpoint clientEndPoint) {
        clientEndPoint.getUserSession().close(new CloseReason(NORMAL_CLOSURE, "Finished test"));
    }

    @LoggingExtensionConfig("com.ruppyrup.server.command.NewGameCommand")
    @Test
    void serverReceivesNewGameCommandWhenNewGameRequested() throws JsonProcessingException, InterruptedException {
        Game game = new Game(GAME_1);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("newGameRoom")
                .gameId(GAME_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("New game with Id " + GAME_1);
        assertThat(gameRepository.getGames().size()).isEqualTo(1);
        assertThat(gameRepository.getGames().getFirst().getGameId()).isEqualTo(game.getGameId());
    }

    @LoggingExtensionConfig("com.ruppyrup.server.command.EnterRoomCommand")
    @Test
    void serverReceivesEnterRoomCommandWhenJoiningGame() throws JsonProcessingException, InterruptedException {
        Game game = new Game(GAME_1);

        gameRepository.addGame(game);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("enterRoom")
                .gameId(GAME_1)
                .playerId(PLAYER_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("Player with Id Player1 entered game with Id " + GAME_1);

        assertThat(gameRepository.getGames().size()).isEqualTo(1);
        assertThat(gameRepository.getGames().getFirst().getGameId()).isEqualTo(game.getGameId());
        assertThat(gameRepository.getGames().getFirst().getPlayers().getFirst().playerId()).isEqualTo(PLAYER_1);
    }

    @Test
    void serverReceivesEnterRoomCommandAndRejectsIfRoomNotValid() throws JsonProcessingException, InterruptedException {
        Game game = new Game(GAME_1);

        gameRepository.addGame(game);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("enterRoom")
                .gameId(GAME_2)
                .playerId(PLAYER_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.ONE_MINUTE)
                .until(() -> !recievedMessages.isEmpty());

        assertThat(recievedMessages.size()).isEqualTo(1);

        DrawPoint received = getMessage(recievedMessages.poll());

        DrawPoint invalid = drawPoint.toBuilder()
                .gameId("Invalid")
                .build();

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(invalid);
    }
}

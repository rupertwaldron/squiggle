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
public class MessageIntegrationTest implements WebSocketClientTrait {
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

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void nonSendingClientReceives() throws JsonProcessingException {
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

        assertThat(recievedMessages.size()).isEqualTo(1);

        DrawPoint received = getMessage(recievedMessages.poll());

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(drawPoint);
    }

    //todo: fix this test
    @Test
    void serverReceivesMouseUpMessage() throws JsonProcessingException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("mouseup")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);
        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !recievedMessages.isEmpty());

        assertThat(recievedMessages.size()).isEqualTo(1);

        DrawPoint received = getMessage(recievedMessages.poll());

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(drawPoint);
    }

    //todo : this test is flakey - not going into safe send
    @Test
    void serverReceivesArtistMessage() throws JsonProcessingException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .playerId(PLAYER_1)
                .guessWord("Pinapple")
                .gameId(GAME_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);
        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.ONE_MINUTE)
                .until(() -> !recievedMessages.isEmpty());

        assertThat(recievedMessages.size()).isEqualTo(1);

        DrawPoint received = getMessage(recievedMessages.poll());

        DrawPoint expected = DrawPoint.builder()
                .action("artist")
                .playerId(PLAYER_1)
                .guessWord("********")
                .gameId(GAME_1)
                .build();

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }


    @LoggingExtensionConfig("com.ruppyrup.server.command.NullCommand")
    @Test
    void serverReceivesInvalidMessage() throws JsonProcessingException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("invalid")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("Null commamd triggered DrawPoint");
    }


    @LoggingExtensionConfig("com.ruppyrup.server.command.NotArtistCommand")
    @Test
    void serverReceivesNotArtistMessage() throws JsonProcessingException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);
        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("Word repository is not set DrawPoint");
    }

    @LoggingExtensionConfig("com.ruppyrup.server.command.MouseUpCommand")
    @Test
    void serverReceivesValidCommandMessage() throws JsonProcessingException, InterruptedException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("mouseup")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("Sending mouse up command");
    }

    @LoggingExtensionConfig("com.ruppyrup.server.command.ArtistCommand")
    @Test
    void serverReceivesNotArtistCommandWhenArtistIsPicked() throws JsonProcessingException, InterruptedException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .playerId(PLAYER_1)
                .guessWord("Banana")
                .gameId(GAME_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("Sending artist change");
    }
}

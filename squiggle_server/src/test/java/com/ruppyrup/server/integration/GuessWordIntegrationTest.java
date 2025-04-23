package com.ruppyrup.server.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import org.json.JSONException;
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
import java.util.List;

import static com.ruppyrup.server.integration.TestUtils.*;
import static com.ruppyrup.server.integration.config.LoggingExtension.listAppender;
import static jakarta.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"reveal.count=2"})
@ExtendWith(LoggingExtension.class)
public class GuessWordIntegrationTest implements WebSocketClientTrait {

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private GameRepository gameRepository;

    @Value("${reveal.count}")
    private int revealCount;


    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() throws JsonProcessingException {
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

    @Test
    void serverReceivesGuessWordWhenArtistIsPicked() throws JsonProcessingException, InterruptedException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> wordRepository.getGuessWord().equals("Monkey") &&
                        wordRepository.getMaskedWord().equals("******") &&
                        recievedMessages.size() == 1);
    }

    @Test
    void serverReceivesWinnerStatusWhenWordGuessed() throws JsonProcessingException, InterruptedException, JSONException {
        wordRepository.setGuessWord("Monkey");
        wordRepository.setIsReady(true);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> recievedMessages.size() == 2);

        DrawPoint expected = DrawPoint.builder()
                .action("winner")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .build();

        assertThat(getMessage(recievedMessages.poll()))
                .usingRecursiveComparison()
                .isEqualTo(expected);

        assertThat(getMessage(recievedMessages.poll()))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void participantReceivesMaskedGuessWord() throws JsonProcessingException, InterruptedException, JSONException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> recievedMessages.size() == 1);

        DrawPoint expected = DrawPoint.builder()
                .action("artist")
                .guessWord("******")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();

        assertThat(getMessage(recievedMessages.poll()))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @LoggingExtensionConfig("com.ruppyrup.server.command.NotArtistCommand")
    @Test
    void participantReceivesNoMaskedWordIsArtisCommandNotRun() throws JsonProcessingException, InterruptedException, JSONException {
        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Invalid")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        for (int i = 0; i < revealCount * 2; i++) {
            clientEndPoints.getFirst().sendMessage(message);
            Thread.sleep(1000);
        }

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> listAppender.list.size() == 4);

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("Word repository is not set");

    }

    @Test
    void participantReceivesMaskedWordWithRevealedCharsAfterXGuesses() throws JsonProcessingException, InterruptedException, JSONException {
        String guessWord = "Monkey";
        DrawPoint artistDrawPoint = DrawPoint.builder()
                .action("artist")
                .guessWord(guessWord)
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();

        String message = mapper.writeValueAsString(artistDrawPoint);
        clientEndPoints.getFirst().sendMessage(message);
        Thread.sleep(1000);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Invalid")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();

        message = mapper.writeValueAsString(drawPoint);

        for (int i = 0; i < revealCount * 6; i++) {
            clientEndPoints.getLast().sendMessage(message);
        }

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> recievedMessages.size() >= 12);

        List<Integer> list = recievedMessages.stream()
                .map(s -> (String) JsonPath.read(s, "$.guessWord"))
                .map(GuessWordIntegrationTest::starMaskCounter)
                .toList();

        assertThat(list)
                .containsExactly(6, 5, 5, 4, 4, 3, 3, 2, 2, 1, 1, 0, 0);
    }

    private static int starMaskCounter(String maskedWord) {
        int countStars = 0;
        for (String s : maskedWord.split("")) {
            if (s.equals("*")) {
                countStars++;
            }
        }
        return countStars;
    }
}

package com.ruppyrup.server.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import com.ruppyrup.server.integration.config.LoggingExtension;
import com.ruppyrup.server.integration.config.LoggingExtensionConfig;
import com.ruppyrup.server.integration.config.WebSocketClientTrait;
import com.ruppyrup.server.integration.config.WebsocketClientEndpoint;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.GuessWord;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.repository.WordRepository;
import jakarta.websocket.CloseReason;
import lombok.SneakyThrows;
import org.json.JSONException;
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

import static com.ruppyrup.server.integration.TestUtils.assertLogMessage;
import static com.ruppyrup.server.integration.TestUtils.getMessage;
import static com.ruppyrup.server.integration.TestUtils.mapper;
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
                .atMost(Duration.ofSeconds(10))
                .until(() -> !recievedMessages.isEmpty());

        GuessWord guessWord = wordRepository.getWord(GAME_1);

        GuessWord expected = new GuessWord("Monkey");
        expected.setIsReady(true);
        expected.setMaskedWord("******");


        assertThat(guessWord)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void serverReceivesWinnerStatusWhenWordGuessed() throws JsonProcessingException, InterruptedException, JSONException {
        wordRepository.addWord(GAME_1, "Monkey");
        GuessWord guessWord = wordRepository.getWord(GAME_1);
        guessWord.setIsReady(true);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> recievedMessages.size() == 2);

        DrawPoint expected = DrawPoint.builder()
                .action("winner")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
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
                .atMost(Duration.ofSeconds(10))
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
        }

        await()
                .atMost(Duration.ofSeconds(60))
                .until(() -> listAppender.list.size() >= 4);

        assertLogMessage("Word repository is not set");
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
                .atMost(Duration.ofSeconds(10))
                .until(() -> recievedMessages.size() >= 12);

        List<Integer> list = recievedMessages.stream()
                .map(s -> (String) JsonPath.read(s, "$.guessWord"))
                .map(GuessWordIntegrationTest::starMaskCounter)
                .toList();

        assertThat(list)
                .containsExactly(6, 5, 5, 4, 4, 3, 3, 2, 2, 1, 1, 0, 0);
    }

    @Test
    void serverReceivesGuessWordWhenArtistIsPickedInOtherRoom() throws JsonProcessingException, InterruptedException {
        twoPlayersEnterTheSameRoom(GAME_2, port, PLAYER_3, PLAYER_4, gameRepository);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> !recievedMessages.isEmpty());

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

    //todo fix this test on word repository
    @Test
    void serverReceivesTwoGuessWordsForDifferentGamesWhenArtistIsPicked() throws JsonProcessingException, InterruptedException {

        // when two players enter the same room and there are two games
        twoPlayersEnterTheSameRoom(GAME_2, port, PLAYER_3, PLAYER_4, gameRepository);

        // and the artist is picked in both games with two different words

        DrawPoint drawPoint1 = DrawPoint.builder()
                .action("artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message1 = mapper.writeValueAsString(drawPoint1);

        clientEndPoints.getFirst().sendMessage(message1);

        DrawPoint drawPoint2 = DrawPoint.builder()
                .action("artist")
                .guessWord("Tap")
                .playerId(PLAYER_4)
                .gameId(GAME_2)
                .build();
        String message2 = mapper.writeValueAsString(drawPoint2);

        clientEndPoints.getLast().sendMessage(message2);

        // then the different games receive the correct masked word

        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> !recievedMessages.isEmpty());

        DrawPoint expected1 = DrawPoint.builder()
                .action("artist")
                .guessWord("******")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();


        DrawPoint expected2 = DrawPoint.builder()
                .action("artist")
                .guessWord("***")
                .playerId(PLAYER_4)
                .gameId(GAME_2)
                .build();

        assertThat(recievedMessages).containsExactlyInAnyOrder(expected1.toJson(), expected2.toJson());

        recievedMessages.clear();

        // then a non-artist guess is received from a players in both games

        DrawPoint drawPoint3 = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();
        String message3 = mapper.writeValueAsString(drawPoint3);

        clientEndPoints.getFirst().sendMessage(message3);

        DrawPoint drawPoint4 = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Tap")
                .playerId(PLAYER_3)
                .gameId(GAME_2)
                .build();
        String message4 = mapper.writeValueAsString(drawPoint4);

        clientEndPoints.getLast().sendMessage(message4);

        await()
                .atMost(Duration.ofSeconds(20))
                .until(() -> recievedMessages.size() >= 4);

        DrawPoint expected3 = DrawPoint.builder()
                .action("winner")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
                .gameId(GAME_1)
                .build();

        DrawPoint expected4 = DrawPoint.builder()
                .action("winner")
                .guessWord("Tap")
                .playerId(PLAYER_3)
                .gameId(GAME_2)
                .build();

        assertThat(recievedMessages).containsExactlyInAnyOrder(expected3.toJson(), expected4.toJson(), expected3.toJson(), expected4.toJson());
        System.out.println("passed");
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

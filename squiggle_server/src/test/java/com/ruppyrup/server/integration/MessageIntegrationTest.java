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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static com.ruppyrup.server.integration.config.LoggingExtension.listAppender;
import static jakarta.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"reveal.count=2"})
@ExtendWith(LoggingExtension.class)
public class MessageIntegrationTest implements WebSocketClientTrait {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String PLAYER_2 = "Player2";
    private static final String PLAYER_1 = "Player1";

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private GameRepository gameRepository;

    @Value("${reveal.count}")
    private int revealCount;


    @LocalServerPort
    private int port;

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

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void nonSendingClientReceives() throws JsonProcessingException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

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
                .atMost(Duration.ONE_MINUTE)
                .until(() -> !recievedMessages.isEmpty());

        assertThat(recievedMessages.size()).isEqualTo(1);

        DrawPoint received = getMessage(recievedMessages.poll());

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(drawPoint);
    }

    @Test
    void serverReceivesMouseUpMessage() throws JsonProcessingException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("mouseup")
                .playerId(PLAYER_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);
        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.ONE_MINUTE)
                .until(() -> !recievedMessages.isEmpty());

        assertThat(recievedMessages.size()).isEqualTo(1);

        DrawPoint received = getMessage(recievedMessages.poll());

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(drawPoint);
    }

    @Test
    void serverReceivesArtistMessage() throws JsonProcessingException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .playerId(PLAYER_1)
                .guessWord("Pinapple")
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
                .build();

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }


    @LoggingExtensionConfig("com.ruppyrup.server.command.NullCommand")
    @Test
    void serverReceivesInvalidMessage() throws JsonProcessingException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("invalid")
                .playerId(PLAYER_1)
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
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .playerId(PLAYER_1)
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
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("mouseup")
                .playerId(PLAYER_1)
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
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .playerId(PLAYER_1)
                .guessWord("Banana")
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("Sending artist change");
    }

    @LoggingExtensionConfig("com.ruppyrup.server.command.NewGameCommand")
    @Test
    void serverReceivesNewGameCommandWhenNewGameRequested() throws JsonProcessingException, InterruptedException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        Game game = Game.builder()
                .gameId("xyz")
                .build();

        DrawPoint drawPoint = DrawPoint.builder()
                .action("newGameRoom")
                .gameId("xyz")
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("New game with Id xyz");
        assertThat(gameRepository.getGames().size()).isEqualTo(1);
        assertThat(gameRepository.getGames().getFirst().gameId()).isEqualTo(game.gameId());
    }

    @LoggingExtensionConfig("com.ruppyrup.server.command.EnterRoomCommand")
    @Test
    void serverReceivesEnterRoomCommandWhenArtistIsPicked() throws JsonProcessingException, InterruptedException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        Game game = Game.builder()
                .gameId("xyz")
                .players(new ArrayList<>())
                .build();

        gameRepository.addGame(game);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("enterRoom")
                .gameId("xyz")
                .playerId(PLAYER_1)
                .build();

        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("Player with Id Player1 entered game with Id xyz");

//        clientEndPoints.getLast().sendMessage(message);
//
//        assertThat(gameRepository.getGames().size()).isEqualTo(1);
//        assertThat(gameRepository.getGames().getFirst().gameId()).isEqualTo(game.gameId());
    }


    @Test
    void serverReceivesGuessWordWhenArtistIsPicked() throws JsonProcessingException, InterruptedException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
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
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        wordRepository.setGuessWord("Monkey");

        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
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
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .guessWord("Monkey")
                .playerId(PLAYER_1)
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
                .build();

        assertThat(getMessage(recievedMessages.poll()))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @LoggingExtensionConfig("com.ruppyrup.server.command.NotArtistCommand")
    @Test
    void participantReceivesNoMaskedWordIsArtisCommandNotRun() throws JsonProcessingException, InterruptedException, JSONException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Invalid")
                .playerId(PLAYER_1)
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
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        String guessWord = "Monkey";
        DrawPoint artistDrawPoint = DrawPoint.builder()
                .action("artist")
                .guessWord(guessWord)
                .playerId(PLAYER_1)
                .build();

        String message = mapper.writeValueAsString(artistDrawPoint);
        clientEndPoints.getFirst().sendMessage(message);
        Thread.sleep(1000);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Invalid")
                .playerId(PLAYER_1)
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
                .map(MessageIntegrationTest::starMaskCounter)
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


    private static DrawPoint getMessage(String receivedMessage) throws JsonProcessingException {
        return mapper.readValue(receivedMessage, DrawPoint.class);
    }
}

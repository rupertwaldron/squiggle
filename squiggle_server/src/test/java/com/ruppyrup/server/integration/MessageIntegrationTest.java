package com.ruppyrup.server.integration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.server.command.NotArtistCommand;
import com.ruppyrup.server.integration.config.LoggingExtension;
import com.ruppyrup.server.integration.config.LoggingExtensionConfig;
import com.ruppyrup.server.integration.config.WebSocketClientTrait;
import com.ruppyrup.server.integration.config.WebsocketClientEndpoint;
import com.ruppyrup.server.model.DrawPoint;
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

import java.util.Arrays;

import static com.ruppyrup.server.integration.config.LoggingExtension.listAppender;
import static jakarta.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"reveal.count=5"})
@ExtendWith(LoggingExtension.class)
public class MessageIntegrationTest implements WebSocketClientTrait {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String PLAYER_2 = "Player2";
    private static final String PLAYER_1 = "Player1";

    @Autowired
    private WordRepository wordRepository;

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
    void listAllBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames); // optional: sort alphabetically

        for (String name : beanNames) {
            System.out.println(name);
        }
    }

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

        DrawPoint received = getMessage(recievedMessages.getFirst());

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

        DrawPoint received = getMessage(recievedMessages.getFirst());
        System.out.println(drawPoint);

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
                .build();

        String message = mapper.writeValueAsString(drawPoint);
        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.ONE_MINUTE)
                .until(() -> !recievedMessages.isEmpty());

        DrawPoint received = getMessage(recievedMessages.getFirst());
        System.out.println(drawPoint);

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(drawPoint);
    }

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
                .atMost(Duration.ONE_MINUTE)
                .until(() -> !recievedMessages.isEmpty());

        DrawPoint received = getMessage(recievedMessages.getFirst());
        System.out.println(drawPoint);

        assertThat(received)
                .usingRecursiveComparison()
                .isEqualTo(drawPoint);
    }

    @LoggingExtensionConfig("com.ruppyrup.server.command.NullCommand")
    @Test
    void serverReceivesInvalidMessage() throws JsonProcessingException, InterruptedException {
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
    void serverReceivesNotArtistCommantWhenArtistIsPicked() throws JsonProcessingException, InterruptedException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("artist")
                .playerId(PLAYER_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        clientEndPoints.getFirst().sendMessage(message);

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> !listAppender.list.isEmpty());

        assertThat(listAppender.list.getFirst().getFormattedMessage())
                .containsSubsequence("Sending artist change");
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
                .until(() -> wordRepository.getGuessWord().equals("Monkey"));
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

        JSONAssert.assertEquals(
                "{\"action\":\"winner\",\"playerId\":\"Player1\",\"guessWord\":\"Monkey\"}",
                recievedMessages.getFirst(),
                JSONCompareMode.LENIENT);

        JSONAssert.assertEquals(
                "{\"action\":\"winner\",\"playerId\":\"Player1\",\"guessWord\":\"Monkey\"}",
                recievedMessages.getLast(),
                JSONCompareMode.LENIENT);
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

        JSONAssert.assertEquals(
                "{\"action\":\"artist\",\"playerId\":\"Player1\",\"x\":0,\"y\":0,\"isFilled\":false,\"guessWord\":\"******\"}",
                recievedMessages.getFirst(),
                JSONCompareMode.LENIENT);
    }

    @Test
    void participantReceivesMaskedWordWithRevealedCharsAfterXGuesses() throws JsonProcessingException, InterruptedException, JSONException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        System.out.println(applicationContext.getBean(NotArtistCommand.class));

        wordRepository.setGuessWord("Monkey");

        DrawPoint drawPoint = DrawPoint.builder()
                .action("not-artist")
                .guessWord("Invalid")
                .playerId(PLAYER_1)
                .build();
        String message = mapper.writeValueAsString(drawPoint);

        for (int i = 0; i < revealCount; i++) {
            clientEndPoints.getFirst().sendMessage(message);
        }

        await()
                .atMost(Duration.TEN_SECONDS)
                .until(() -> recievedMessages.size() == 1);

        JSONAssert.assertEquals(
                "{\"action\":\"reveal\",\"playerId\":\"Player1\",\"x\":0,\"y\":0,\"isFilled\":false,\"guessWord\":\"*****y\"}",
                recievedMessages.getFirst(),
                JSONCompareMode.LENIENT);
    }


    private static DrawPoint getMessage(String receivedMessage) throws JsonProcessingException {
        return mapper.readValue(receivedMessage, DrawPoint.class);
    }
}

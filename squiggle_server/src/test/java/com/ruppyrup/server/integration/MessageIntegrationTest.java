package com.ruppyrup.server.integration;


import com.ruppyrup.server.command.NullCommand;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.ruppyrup.server.command.SquiggleCommandFactory;
import com.ruppyrup.server.integration.config.WebSocketClientTrait;
import com.ruppyrup.server.integration.config.WebsocketClientEndpoint;
import com.ruppyrup.server.model.DrawPoint;
import jakarta.websocket.CloseReason;
import lombok.SneakyThrows;
import org.awaitility.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static jakarta.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageIntegrationTest implements WebSocketClientTrait {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final TypeFactory typeFactory = mapper.getTypeFactory();
    ;
    private static final String PLAYER_2 = "Player2";
    private static final String PLAYER_1 = "Player1";

    @Autowired
    private SquiggleCommandFactory squiggleCommandFactory;

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

    @Test
    void nonSendingClientReceives() throws JsonProcessingException {
        connectWebsocketClient(port);
        connectWebsocketClient(port);

        DrawPoint drawPoint = DrawPoint.builder()
                .action("draw")
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
                .action("mouseUp")
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

    @Test
    void serverReceivesInvalidMessage() throws JsonProcessingException, InterruptedException {
        Logger commandLogger = (Logger) LoggerFactory.getLogger(NullCommand.class);

        // create and start a ListAppender
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        commandLogger.addAppender(listAppender);

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

    private static DrawPoint getMessage(String receivedMessage) throws JsonProcessingException {
        return mapper.readValue(receivedMessage, DrawPoint.class);
    }
}

package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.WordMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Slf4j
public class NewGameCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final GameRepository gameRepository;

    public NewGameCommand(MessageService messageService, GameRepository gameRepository) {
        this.messageService = messageService;
        this.gameRepository = gameRepository;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        String gameId = drawPoint.gameId();
        DrawPoint drawPointToSend = DrawPoint.builder()
                .action("invalid")
                .gameId(gameId)
                .build();

        if (gameRepository.gameExists(gameId)) {
            log.info("Game with Id {} already exists on thread {}", gameId, Thread.currentThread());
            drawPointToSend = drawPointToSend.toBuilder()
                    .action("gameExistsAlready")
                    .build();
        } else {
            Game game = new Game(gameId);
            gameRepository.addGame(game);
            drawPointToSend = drawPointToSend.toBuilder()
                    .action("gameCreated")
                    .build();
            log.info("New game with Id {} added on thread {}", gameId, Thread.currentThread());
        }

        try {
            sendBackToSession(session, drawPointToSend, gameRepository, messageService);
//            messageService.sendInfoToSessions(List.of(session), drawPointToSend.toJson());
        } catch (IllegalStateException e) {
            log.warn("No sessions found for game id {} on thread {}: {}", drawPoint.gameId(), Thread.currentThread(), e.getMessage());
        }
    }
}

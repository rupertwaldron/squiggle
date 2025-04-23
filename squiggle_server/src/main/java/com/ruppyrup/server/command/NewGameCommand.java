package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.WordMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

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
        Game game = new Game(drawPoint.gameId());

        gameRepository.addGame(game);

        log.info("New game with Id {} added on thread {}", game.getGameId(), Thread.currentThread());
    }
}

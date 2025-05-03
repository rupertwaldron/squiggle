package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.model.GuessWord;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.WordMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;

@Slf4j
public class ArtistCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final WordRepository wordRepository;
    private final GameRepository gameRepository;

    public ArtistCommand(MessageService messageService, WordRepository wordRepository, GameRepository gameRepository) {
        this.messageService = messageService;
        this.wordRepository = wordRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        String guessWord = drawPoint.guessWord();
        String gameId = drawPoint.gameId();
        wordRepository.addWord(gameId, guessWord);
        GuessWord guessWordObj = wordRepository.getWord(gameId);

        String maskedWord = WordMasker.getMaskedWord(guessWord, guessWordObj.getMaskedWord(),0);
        guessWordObj.setMaskedWord(maskedWord);
        guessWordObj.setIsReady(true);

        DrawPoint drawPointToSend = DrawPoint.builder()
                .action(drawPoint.action())
                .playerId(drawPoint.playerId())
                .guessWord(maskedWord)
                .gameId(gameId)
                .build();

        List<WebSocketSession> sessions = getGameSessions(drawPoint, gameRepository);

        if (sessions.isEmpty()) {
            log.warn("No sessions found for game id {} on thread {}", gameId, Thread.currentThread());
            return;
        }

        sessions.remove(session);

        try {
            messageService.sendInfoToSessions(sessions, drawPointToSend.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Sending artist change {} on thread {}", drawPoint, Thread.currentThread());
    }
}

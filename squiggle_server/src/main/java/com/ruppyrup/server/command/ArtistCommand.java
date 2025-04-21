package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.model.Player;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.SessionUtils;
import com.ruppyrup.server.utils.WordMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

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
        wordRepository.setGuessWord(guessWord);

        String maskedWord = WordMasker.getMaskedWord(guessWord, wordRepository.getMaskedWord(),0);
        wordRepository.setMaskedWord(maskedWord);
        wordRepository.setIsReady(true);

        DrawPoint drawPointToSend = DrawPoint.builder()
                .action(drawPoint.action())
                .playerId(drawPoint.playerId())
                .guessWord(maskedWord)
                .build();

        if (!gameRepository.gameExists(drawPoint.gameId())) {
            log.info("Game with Id does not exist");
            return;
        }

        List<WebSocketSession> sessions = SessionUtils.getGameSessions(drawPoint,gameRepository);

        try {
            messageService.sendInfoToSessions(sessions, drawPointToSend.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Sending artist change {} on thread {}", drawPoint, Thread.currentThread());
    }
}

package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.WordMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;

@Slf4j
public class NotArtistCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final WordRepository wordRepository;
    private final GameRepository gameRepository;
    private final int revealTriggerPoint;

    public NotArtistCommand(MessageService messageService, WordRepository wordRepository, GameRepository gameRepository, int revealTriggerPoint) {
        this.messageService = messageService;
        this.wordRepository = wordRepository;
        this.gameRepository = gameRepository;
        this.revealTriggerPoint = revealTriggerPoint;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        if (wordRepository.getGuessWord() == null || !wordRepository.isReady()) {
            log.info("Word repository is not set {} on thread {}", drawPoint, Thread.currentThread());
            return;
        }
        List<WebSocketSession> sessions = getGameSessions(drawPoint, gameRepository);

        if (sessions.isEmpty()) {
            log.warn("No sessions found for game id {} on thread {}", drawPoint.gameId(), Thread.currentThread());
            return;
        }

        if (wordRepository.getGuessWord().equalsIgnoreCase(drawPoint.guessWord())) {
            handleWinner(drawPoint, sessions);
        } else {
            handleRetry(drawPoint, sessions);
        }
    }

    private void handleRetry(DrawPoint drawPoint, List<WebSocketSession> sessions) {
        wordRepository.incrementGuessCount();
        if (wordRepository.getGuessCount() >= revealTriggerPoint) {
            log.info("Reveal another letter {} on thread {}", drawPoint, Thread.currentThread());
            wordRepository.incrementRevealCount();
            String maskedWord = WordMasker.getMaskedWord(wordRepository.getGuessWord(), wordRepository.getMaskedWord(), wordRepository.getRevealCount());
            wordRepository.setMaskedWord(maskedWord);
            log.info("Masked word is {} on thread {}", maskedWord, Thread.currentThread());
            DrawPoint drawPointToSend = DrawPoint.builder()
                    .action("reveal")
                    .playerId(drawPoint.playerId())
                    .guessWord(maskedWord)
                    .build();

            try {
                messageService.sendInfoToSessions(sessions, drawPointToSend.toJson());
                wordRepository.setGuessCount(0);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleWinner(DrawPoint drawPoint, List<WebSocketSession> sessions) {
        log.info("Correct guess {} on thread {}", drawPoint, Thread.currentThread());
        DrawPoint winnerDrawPoint = DrawPoint.builder()
                .action("winner")
                .playerId(drawPoint.playerId())
                .guessWord(wordRepository.getGuessWord())
                .build();

        try {
            messageService.sendInfoToSessions(sessions, winnerDrawPoint.toJson());
            wordRepository.reset();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

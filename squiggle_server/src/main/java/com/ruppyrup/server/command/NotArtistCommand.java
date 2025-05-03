package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.GuessWord;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.WordMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

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
        String gameId = drawPoint.gameId();
        GuessWord guessWord = wordRepository.getWord(gameId);
        if (guessWord == null || !guessWord.isReady()) {
            log.info("Word repository is not set {} on thread {}", drawPoint, Thread.currentThread());
            return;
        }
        List<WebSocketSession> sessions = getGameSessions(drawPoint, gameRepository);

        if (sessions.isEmpty()) {
            log.warn("No sessions found for game id {} on thread {}", drawPoint.gameId(), Thread.currentThread());
            return;
        }

        if (guessWord.getGuessWord().equalsIgnoreCase(drawPoint.guessWord())) {
            handleWinner(drawPoint, sessions, guessWord);
        } else {
            handleRetry(drawPoint, sessions, guessWord);
        }
    }

    private void handleRetry(DrawPoint drawPoint, List<WebSocketSession> sessions, GuessWord guessWord) {
        guessWord.incrementGuessCount();
        if (guessWord.getGuessCount() >= revealTriggerPoint) {
            log.info("Reveal another letter {} on thread {}", drawPoint, Thread.currentThread());
            guessWord.incrementRevealCount();
            String maskedWord = WordMasker.getMaskedWord(guessWord.getGuessWord(), guessWord.getMaskedWord(), guessWord.getRevealCount());
            guessWord.setMaskedWord(maskedWord);
            log.info("Masked word is {} on thread {}", maskedWord, Thread.currentThread());
            DrawPoint drawPointToSend = DrawPoint.builder()
                    .action("reveal")
                    .playerId(drawPoint.playerId())
                    .guessWord(maskedWord)
                    .build();

            try {
                messageService.sendInfoToSessions(sessions, drawPointToSend.toJson());
                guessWord.setGuessCount(0);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleWinner(DrawPoint drawPoint, List<WebSocketSession> sessions, GuessWord guessWord) {
        log.info("Correct guess {} on thread {}", drawPoint, Thread.currentThread());
        DrawPoint winnerDrawPoint = DrawPoint.builder()
                .action("winner")
                .playerId(drawPoint.playerId())
                .guessWord(guessWord.getGuessWord())
                .build();

        try {
            messageService.sendInfoToSessions(sessions, winnerDrawPoint.toJson());
            guessWord.reset();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

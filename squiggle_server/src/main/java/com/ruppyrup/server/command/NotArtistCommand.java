package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.WordMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class NotArtistCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final WordRepository wordRepository;
    private final int revealTriggerPoint;

    public NotArtistCommand(MessageService messageService, WordRepository wordRepository, int revealTriggerPoint) {
        this.messageService = messageService;
        this.wordRepository = wordRepository;
        this.revealTriggerPoint = revealTriggerPoint;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        if (wordRepository.getGuessWord() == null || !wordRepository.isReady()) {
            log.info("Word repository is not set {} on thread {}", drawPoint, Thread.currentThread());
            return;
        }

        if (wordRepository.getGuessWord().equalsIgnoreCase(drawPoint.guessWord())) {
            handleWinner(drawPoint);
        } else {
            handleRetry(drawPoint);
        }
    }

    private void handleRetry(DrawPoint drawPoint) {
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
                messageService.sendInfoToAll(drawPointToSend.toJson());
                wordRepository.setGuessCount(0);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleWinner(DrawPoint drawPoint) {
        log.info("Correct guess {} on thread {}", drawPoint, Thread.currentThread());
        DrawPoint winnerDrawPoint = DrawPoint.builder()
                .action("winner")
                .playerId(drawPoint.playerId())
                .guessWord(wordRepository.getGuessWord())
                .build();
        try {
            messageService.sendInfoToAll(winnerDrawPoint.toJson());
            wordRepository.setGuessWord(null);
            wordRepository.setGuessCount(0);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

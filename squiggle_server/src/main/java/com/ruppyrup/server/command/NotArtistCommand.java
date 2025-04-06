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
    private final int revealCount;

    public NotArtistCommand(MessageService messageService, WordRepository wordRepository, int revealCount) {
        this.messageService = messageService;
        this.wordRepository = wordRepository;
        this.revealCount = revealCount;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        if (wordRepository.getGuessWord() == null) {
            log.info("Guess word is null {} on thread {}", drawPoint, Thread.currentThread());
            return;
        }

        if (wordRepository.getGuessWord().equalsIgnoreCase(drawPoint.guessWord())) {
            handleWinner(session, drawPoint);
        } else {
            handleRetry(session, drawPoint);
        }
    }

    private void handleRetry(WebSocketSession session, DrawPoint drawPoint) {
        wordRepository.incrementGuessCount();
        if (wordRepository.getGuessCount() >= revealCount) {
            log.info("Reveal another letter {} on thread {}", drawPoint, Thread.currentThread());
            wordRepository.incrementRevealCount();
//            todo check this is sending
//            todo need to remember the letters that have been revealed
            String maskedWord = WordMasker.getMaskedWord(wordRepository.getGuessWord(), wordRepository.getMaskedWord(), wordRepository.getRevealCount());
            wordRepository.setMaskedWord(maskedWord);
            log.info("Masked word is {} on thread {}", maskedWord, Thread.currentThread());
            DrawPoint drawPointToSend = DrawPoint.builder()
                    .action("reveal")
                    .playerId(drawPoint.playerId())
                    .guessWord(maskedWord)
                    .build();
            try {
                messageService.sendInfo(session, drawPointToSend.toJson());
                wordRepository.setGuessCount(0);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleWinner(WebSocketSession session, DrawPoint drawPoint) {
        log.info("Correct guess {} on thread {}", drawPoint, Thread.currentThread());
        DrawPoint winnerDrawPoint = DrawPoint.builder()
                .action("winner")
                .playerId(drawPoint.playerId())
                .guessWord(wordRepository.getGuessWord())
                .build();
        try {
            messageService.sendInfo(session, winnerDrawPoint.toJson());
            wordRepository.setGuessWord(null);
            wordRepository.setGuessCount(0);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

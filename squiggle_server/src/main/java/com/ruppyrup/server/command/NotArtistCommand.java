package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.WordMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class NotArtistCommand implements SquiggleCommand {

    @Value("${reveal.count:4}")
    private int revealCount;

    private final MessageService messageService;
    private final WordRepository wordRepository;

    public NotArtistCommand(MessageService messageService, WordRepository wordRepository) {
        this.messageService = messageService;
        this.wordRepository = wordRepository;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        if (wordRepository.getGuessWord() == null) {
            log.info("Guess word is null {} on thread {}", drawPoint, Thread.currentThread());
            return;
        }

        if (wordRepository.getGuessWord().equalsIgnoreCase(drawPoint.guessWord())) {
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
        } else {
            wordRepository.incrementGuessCount();
            if (wordRepository.getGuessCount() >= 4) {
                log.info("Reveal another letter {} on thread {}", drawPoint, Thread.currentThread());
                DrawPoint drawPointToSend = DrawPoint.builder()
                        .action("reveal")
                        .playerId(drawPoint.playerId())
                        .guessWord(WordMasker.getMaskedWord(wordRepository.getGuessWord(), wordRepository.getGuessCount() / 4))
                        .build();
                try {
                    messageService.sendInfo(session, drawPointToSend.toJson());
                    wordRepository.setGuessCount(0);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

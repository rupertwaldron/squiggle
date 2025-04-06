package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.WordMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class ArtistCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final WordRepository wordRepository;

    public ArtistCommand(MessageService messageService, WordRepository wordRepository) {
        this.messageService = messageService;
        this.wordRepository = wordRepository;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        String guessWord = drawPoint.guessWord();
        wordRepository.setGuessWord(guessWord);

        String maskedWord = WordMasker.getMaskedWord(guessWord, wordRepository.getMaskedWord(),0);

        DrawPoint drawPointToSend = DrawPoint.builder()
                .action(drawPoint.action())
                .playerId(drawPoint.playerId())
                .guessWord(maskedWord)
                .build();

        try {
            messageService.sendInfo(session, drawPointToSend.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Sending artist change {} on thread {}", drawPoint, Thread.currentThread());
    }
}

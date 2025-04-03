package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class NotArtistCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final WordRepository wordRepository;

    public NotArtistCommand(MessageService messageService, WordRepository wordRepository) {
        this.messageService = messageService;
        this.wordRepository = wordRepository;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        if (wordRepository.getGuessWord() == null) return;

        if (wordRepository.getGuessWord().equalsIgnoreCase(drawPoint.guessWord())) {
            DrawPoint winnerDrawPoint = DrawPoint.builder()
                    .action("winner")
                    .playerId(drawPoint.playerId())
                    .guessWord(wordRepository.getGuessWord())
                    .build();
            try {
                messageService.sendInfo(session, winnerDrawPoint.toJson());
                wordRepository.setGuessWord(null);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("Sending artist change {} on thread {}", drawPoint, Thread.currentThread());
    }
}

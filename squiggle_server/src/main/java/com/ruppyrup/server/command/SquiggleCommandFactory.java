package com.ruppyrup.server.command;

import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SquiggleCommandFactory {
    private final Map<String, SquiggleCommand> commandMap = new HashMap<>();

    public SquiggleCommandFactory(MessageService messageService, WordRepository wordRepository, int revealCount) {
        this.commandMap.put("mousemove", new MouseDownCommand(messageService));
        this.commandMap.put("mouseup", new MouseUpCommand(messageService));
        this.commandMap.put("artist", new ArtistCommand(messageService, wordRepository));
        this.commandMap.put("not-artist", new NotArtistCommand(messageService, wordRepository, revealCount));
    }

    public SquiggleCommand getCommand(String command) {
        SquiggleCommand squiggleCommand = commandMap.get(command);
        if (squiggleCommand == null) {
            return new NullCommand();
        }
        return squiggleCommand;
    }
}

package com.ruppyrup.server.command;

import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class SquiggleCommandFactory {
    private final Map<String, SquiggleCommand> commandMap = new HashMap<>();

    public SquiggleCommandFactory(MessageService messageService) {
        this.commandMap.put("mousemove", new MouseDownCommand(messageService));
        this.commandMap.put("mouseup", new MouseUpCommand(messageService));
        this.commandMap.put("artist", new ArtistCommand(messageService));
        this.commandMap.put("not-artist", new ArtistCommand(messageService));
    }

    public SquiggleCommand getCommand(String command) {
        SquiggleCommand squiggleCommand = commandMap.get(command);
        if (squiggleCommand == null) {
            return new NullCommand();
        }
        return squiggleCommand;
    }
}

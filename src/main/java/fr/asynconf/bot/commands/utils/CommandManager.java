package fr.asynconf.bot.commands.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CommandManager {
    
    private List<Command> commands = new ArrayList<>();
    
    public void addCommand(Command command) {
        this.commands.add(command);
    }
    
    public void addCommands(Command... commands) {
        this.commands.addAll(List.of(commands));
    }
    
    public Iterator<Command> getByName(String name) {
        return this.commands.stream()
                .filter(a -> a.name().equalsIgnoreCase(name))
                .iterator();
    }
    
}

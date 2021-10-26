package fr.asynconf.bot.commands.utils;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.IOException;
import java.sql.SQLException;

public interface CommandExecutor {
    
    void run(SlashCommandEvent event) throws SQLException, IOException;
    
}

package fr.asynconf.bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;

public class ErrorEmbed extends EmbedBuilder {
    
    public ErrorEmbed() {
        setColor(0xeb3b5a);
        setTitle("Erreur !");
    }
}

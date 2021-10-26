package fr.asynconf.bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;

public class SuccessEmbed extends EmbedBuilder {
    
    public SuccessEmbed() {
        setColor(0x2ed573);
        setTitle("Succès !");
    }
}

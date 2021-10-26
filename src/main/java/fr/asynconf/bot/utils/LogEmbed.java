package fr.asynconf.bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;

public class LogEmbed extends EmbedBuilder {
    
    public LogEmbed(User member) {
        setFooter("ID : " + member.getId());
        setAuthor(member.getName(), member.getAvatarUrl(), member.getAvatarUrl());
        setTimestamp(Instant.now());
    }
    
    public LogEmbed(User member, User moderator) {
        this(member);
        setAuthor(moderator.getName(), moderator.getAvatarUrl(), moderator.getAvatarUrl());
    }
}

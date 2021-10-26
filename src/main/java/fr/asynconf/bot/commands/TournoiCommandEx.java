package fr.asynconf.bot.commands;

import fr.asynconf.bot.Main;
import fr.asynconf.bot.commands.utils.CommandExecutor;
import fr.asynconf.bot.utils.ErrorEmbed;
import fr.asynconf.bot.utils.LogEmbed;
import fr.asynconf.bot.utils.SuccessEmbed;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.IOException;

public class TournoiCommandEx implements CommandExecutor {
    
    private final Main main;
    private final TextChannel logChannel;
    
    public TournoiCommandEx(Main main) {
        this.main = main;
        Guild guild = main.getClient().getGuildById(main.getConfig().getLong("bot.guild.id"));
        this.logChannel = guild.getTextChannelById(main.getConfig().getLong("bot.guild.log_channel.id"));
    }
    
    @Override
    public void run(SlashCommandEvent event) throws IOException {
    
        Member member = event.getGuild().retrieveMember(event.getUser()).complete();
        
        if (!member.hasPermission(Permission.BAN_MEMBERS)) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Vous n'avez pas la permission d'exécuter cette commande !")
                            .build())
                    .setEphemeral(true)
                    .queue();
            logChannel.sendMessage(new LogEmbed(event.getUser())
                    .setTitle("Alerte !")
                    .setDescription(
                            member.getAsMention() + " a essayé d'exécuter la commande : `/tournoi` alors qu'il n'en a pas la permission!"
                    )
                    .setColor(0xa55eea)
                    .build()
            ).queue();
            return;
        }
    
        switch (event.getSubcommandName()) {
            case "start" -> {
                this.main.getConfig().set("bot.guild.submissions.state", "opened");
                this.main.getConfig().save(this.main.getConfigFile());
                event.replyEmbeds(new SuccessEmbed()
                                .setTitle("Soumissions ouvertes")
                                .setDescription("Les soumissions ont bien été ouvertes !")
                                .build())
                        .setEphemeral(true)
                        .queue();
                logChannel.sendMessage(new LogEmbed(event.getUser())
                                .setDescription("Les soumissions de projet ont été ouvertes !")
                                .setColor(0x2ed573)
                                .build())
                        .queue();
            }
            case "end" -> {
                this.main.getConfig().set("bot.guild.submissions.state", "closed");
                this.main.getConfig().save(this.main.getConfigFile());
                event.replyEmbeds(new ErrorEmbed()
                                .setTitle("Soumissions fermées")
                                .setDescription("Les soumissions ont bien été fermées !")
                                .build())
                        .setEphemeral(true)
                        .queue();
                logChannel.sendMessage(new LogEmbed(event.getUser())
                                .setDescription("Les soumissions de projet ont été fermées !")
                                .setColor(0xeb3b5a)
                                .build())
                        .queue();
            }
            default -> event.reply("Error ! Cannot resolve the used subcommand !")
                    .setEphemeral(true)
                    .queue();
        }
        
    }
}

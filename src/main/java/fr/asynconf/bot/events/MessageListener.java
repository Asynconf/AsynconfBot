package fr.asynconf.bot.events;

import fr.asynconf.bot.Main;
import fr.asynconf.bot.commands.BanCommandEx;
import fr.asynconf.bot.commands.ProjectCommandEx;
import fr.asynconf.bot.commands.SubmitCommandEx;
import fr.asynconf.bot.commands.TournoiCommandEx;
import fr.asynconf.bot.commands.utils.Command;
import fr.asynconf.bot.commands.utils.CommandManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class MessageListener extends ListenerAdapter {
    
    private final TextChannel depositChannel;
    private final CommandManager manager;
    
    public MessageListener(Main main) {
        Guild guild = main.getClient().getGuildById(main.getConfig().getLong("bot.guild.id"));
        this.depositChannel = guild.getTextChannelById(main.getConfig().getLong("bot.guild.submissions.deposit_id"));
        
        this.manager = new CommandManager();
        this.manager.addCommands(
                new Command("tournoi", new TournoiCommandEx(main)),
                new Command("ban", new BanCommandEx(main)),
                new Command("submit", new SubmitCommandEx(main)),
                new Command("project", new ProjectCommandEx(main))
        );
    }
    
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getIdLong() == depositChannel.getIdLong()) {
            if (event.getAuthor().isBot()) return;
            if (event.isWebhookMessage()) return;
            if (Objects.requireNonNull(event.getGuild().retrieveMember(event.getAuthor()))
                    .complete()
                    .hasPermission(Permission.BAN_MEMBERS)
            )
                return;
            event.getMessage().delete().queue();
        }
    }
    
    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        
        this.manager.getByName(event.getName())
                .forEachRemaining(re -> {
                    try {
                        re.executor().run(event);
                    } catch (SQLException | IOException e) {
                        event.reply("Erreur : " + e.getClass().getSimpleName() + " ; " + e.getMessage())
                                .setEphemeral(true)
                                .setTTS(true)
                                .queue();
                        e.printStackTrace();
                    }
                });
        
    }
}

/*
 * The Asynconf Bot is a bot with the purpose of managing the members and the 'tournoi' of the conference.
 *
 *     Asynconf Bot  Copyright (C) 2021  RedsTom
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.asynconf.bot.commands;

import fr.asynconf.bot.Main;
import fr.asynconf.bot.commands.utils.CommandExecutor;
import fr.asynconf.bot.utils.ErrorEmbed;
import fr.asynconf.bot.utils.LogEmbed;
import fr.asynconf.bot.utils.ProjectEmbed;
import fr.asynconf.bot.utils.SuccessEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectCommandEx implements CommandExecutor {
    
    private final Main main;
    private final TextChannel logChannel;
    private final TextChannel reviewChannel;
    
    public ProjectCommandEx(Main main) {
        this.main = main;
        Guild guild = main.getClient().getGuildById(main.getConfig().getLong("bot.guild.id"));
        this.logChannel = guild.getTextChannelById(main.getConfig().getLong("bot.guild.log_channel.id"));
        this.reviewChannel = guild.getTextChannelById(main.getConfig().getLong("bot.guild.submissions.review_id"));
    }
    
    @Override
    public void run(SlashCommandEvent event) throws SQLException {
        
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
        
        
        OptionMapping oIp = event.getOption("id");
        if (oIp == null) {
            event.reply("Error ! The option `id` is empty !")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        long id = oIp.getAsLong();
        
        PreparedStatement ps = this.main.getDbConnector().getConnection()
                .prepareStatement("""
                        SELECT * FROM submissions WHERE id=?
                        """);
        ps.setLong(1, id);
        
        ResultSet rs = ps.executeQuery();
        
        if (!rs.next()) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Impossible de trouver un projet avec l'id `" + id + "`")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (rs.getString("state").equalsIgnoreCase("success")) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Ce projet a déjà été accepté !")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (rs.getString("state").equalsIgnoreCase("failure")) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Ce projet a déjà été refusé !")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        List<MessageEmbed> embeds = new ArrayList<>();
        Message msg = this.reviewChannel.retrieveMessageById(rs.getLong("message_id")).complete();
        
        if (msg == null) {
            embeds.add(new ErrorEmbed()
                    .setDescription(":warning: Erreur non critique\n" +
                            "Le message d'annonce du projet n'a pas été trouvé !")
                    .build());
        }
        
        User user = this.main.getClient().retrieveUserById(rs.getLong("user_id"), true).complete();
        
        if (user == null) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("L'utilisateur lié au projet n'est pas trouvable !")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        PreparedStatement ps2 = this.main.getDbConnector().getConnection()
                .prepareStatement("""
                        UPDATE submissions SET state=? WHERE id=?
                        """);
        ps2.setString(1, Objects.equals(event.getSubcommandName(), "accept") ? "success" : "failure");
        ps2.setLong(2, id);
        
        ps2.execute();
        
        if (msg != null) {
            msg.editMessage(
                            new ProjectEmbed(
                                    event.getUser(),
                                    rs.getString("repl_link"),
                                    Math.toIntExact(id),
                                    Objects.equals(event.getSubcommandName(), "accept") ? "success" : "failure")
                                    .build())
                    .queue();
        }
        
        
        switch (event.getSubcommandName()) {
            case "accept" -> {
                user.openPrivateChannel().complete().sendMessage(new SuccessEmbed()
                                .setTitle("Projet accepté !")
                                .setDescription("Votre projet avec l'identifiant `" + id + "` a été accepté")
                                .build())
                        .queue();
                embeds.add(new SuccessEmbed()
                        .setDescription("Le projet avec l'identifiant `" + id + "` a bien été accepté")
                        .build());
            }
            case "deny" -> {
                OptionMapping oReason = event.getOption("reason");
                if (oReason == null) {
                    event.reply("Error ! The `reason` option is empty !")
                            .setEphemeral(true)
                            .queue();
                    return;
                }
                
                user.openPrivateChannel().complete().sendMessage(new ErrorEmbed()
                                .setTitle("Projet refusé !")
                                .setDescription("Votre projet avec l'identifiant `" + id + "` a été refusé")
                                .addField("Raison :", oReason.getAsString(), false)
                                .build())
                        .queue();
                
                embeds.add(new SuccessEmbed()
                        .setDescription("Le projet avec l'identifiant `" + id + "` a bien été refusé")
                        .build());
            }
        }
        
        event.replyEmbeds(embeds.get(0), embeds.stream().skip(1).toArray(MessageEmbed[]::new))
                .setEphemeral(true)
                .queue();
        
    }
    
}

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
import fr.asynconf.bot.utils.SuccessEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BanCommandEx implements CommandExecutor {
    
    private final TextChannel logChannel;
    
    public BanCommandEx(Main main) {
        Guild asynconfGuild = main.getClient().getGuildById(
                main.getConfig().getLong("bot.guild.id"));
        this.logChannel = asynconfGuild.getTextChannelById(
                main.getConfig().getLong("bot.guild.log_channel.id"));
    }
    
    @Override
    public void run(SlashCommandEvent event) {
        
        User user = event.getUser();
        Guild guild = event.getGuild();
        
        Member member = guild.retrieveMember(user).complete();
        
        if (!member.hasPermission(Permission.BAN_MEMBERS)) {
            
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Vous n'avez pas la permission d'exécuter cette commande !")
                            .build()
                    )
                    .setEphemeral(true)
                    .queue();
            
            logChannel.sendMessage(new LogEmbed(user)
                            .setTitle("Alerte !")
                            .setDescription(
                                    member.getAsMention() + " a essayé d'exécuter la commande : `/ban` alors qu'il n'en a pas la permission!"
                            )
                            .setColor(0xa55eea)
                            .build()
                    )
                    .queue();
            
            return;
        }
        
        
        OptionMapping oMemberToBan = event.getOption("user");
        
        if (oMemberToBan == null) {
            event.reply("Error ! The `user` option is empty !")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        Member memberToBan = oMemberToBan.getAsMember();
        if (memberToBan == null) {
            event.reply("Error ! The `user` option is null. Please retry later...")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (!member.canInteract(memberToBan)) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("`$ sudo ban`\nError : Not enough permission to ban " + memberToBan.getAsMention())
                            .build()
                    )
                    .setEphemeral(true)
                    .queue();
            
            return;
        }
        
        OptionMapping oReason = event.getOption("reason");
        if (oReason == null) {
            event.reply("Error ! The `reason` option is empty !")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        String reason = oReason.getAsString();
        if (reason.contains("Autre")) {
            OptionMapping oOtherReason = event.getOption("other-reason");
            if (oOtherReason == null) {
                event.replyEmbeds(new ErrorEmbed()
                                .setDescription("Lorsque `Autre` est sélectionné dans le champ `reason`," +
                                        " alors le champ `other-option` devient obligatoire !")
                                .build()
                        )
                        .setEphemeral(true)
                        .queue();
                
                return;
            }
            reason = oOtherReason.getAsString();
        }
        
        String finalReason = reason;
        memberToBan.getUser().openPrivateChannel()
                .complete()
                .sendMessage(
                        new EmbedBuilder()
                                .setTitle("Notification")
                                .setDescription("Vous avez été banni du discord de l'Asynconf")
                                .addField("Raison :", reason, false)
                                .setColor(0xeccc68)
                                .build()
                )
                .queue(msg -> finalBan(event, member, memberToBan, finalReason));
    }
    
    private void finalBan(SlashCommandEvent event, Member member, Member memberToBan, String finalReason) {
        memberToBan.ban(0, String.format("""
                                Raison : %s ;
                                Banni par : %s ;
                                Date : %s
                                """.stripIndent(),
                        finalReason,
                        member.getUser().getName() + "#" + member.getUser().getDiscriminator(),
                        event.getTimeCreated()
                                .atZoneSameInstant(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss"))
                ))
                .queue();
        event.replyEmbeds(new SuccessEmbed()
                        .setDescription("L'utilisateur `"
                                + memberToBan.getEffectiveName()
                                + "#"
                                + memberToBan.getUser().getDiscriminator()
                                + "` a bien été banni pour la raison : "
                                + "`" + finalReason + "`")
                        .build()
                )
                .setEphemeral(true)
                .queue();
        logChannel.sendMessage(new LogEmbed(memberToBan.getUser(), member.getUser())
                        .setTitle("Bannissement")
                        .setDescription("L'utilisateur `"
                                + memberToBan.getEffectiveName()
                                + "#"
                                + memberToBan.getUser().getDiscriminator()
                                + "`"
                                + " a été banni pour : `" + finalReason + "`")
                        .setColor(0xff4757)
                        .build())
                .queue();
    }
}

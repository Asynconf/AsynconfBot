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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.IOException;
import java.sql.SQLException;

public class StatusCommandEx implements CommandExecutor {
    public StatusCommandEx(Main main) {
    }
    
    @Override
    public void run(SlashCommandEvent event) throws SQLException, IOException {
    
        Member member = event.getGuild().retrieveMember(event.getUser()).complete();
    
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Vous n'avez pas la permission d'exécuter cette commande !")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
    
        event.getJDA().getPresence().setActivity(Activity.playing(event.getOption("status").getAsString()));
        event.reply("Le status du bot a bien été mis à jour sur : `" + event.getOption("status").getAsString() + "`.")
                .setEphemeral(true)
                .queue();
    }
}

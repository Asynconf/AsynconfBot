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

package fr.asynconf.bot.utils;

import net.dv8tion.jda.api.entities.User;

public class ProjectEmbed extends LogEmbed {
    public ProjectEmbed(User member, String projectUrl, int insertedId, String insertedState) {
        super(member);
        retrieveColor(insertedState);
        addField("URL : ", projectUrl, false);
        addField("ID : ", "" + insertedId, true);
        addField("Status : ", retrieveState(insertedState), true);
    }
    
    private String retrieveState(String insertedState) {
        return switch (insertedState) {
            case "pending" -> "<:pending:902614613772861481> En attente...";
            case "taken" -> "<:question:903316900627484683> Pris en charge...";
            case "success" -> "<:yes:902614739090305054> Accepté !";
            case "failure" -> "<:no:902614635889455124> Refusé !";
            default -> "??? " + insertedState;
        };
    }
    
    private void retrieveColor(String insertedState) {
        switch (insertedState) {
            case "pending" -> setColor(0x9c88ff);
            case "success" -> setColor(0x2ed573);
            case "failure" -> setColor(0xeb3b5a);
            case "taken" -> setColor(0xfeca57);
        }
    }
}

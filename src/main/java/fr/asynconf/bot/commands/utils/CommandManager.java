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

package fr.asynconf.bot.commands.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CommandManager {
    
    private List<Command> commands = new ArrayList<>();
    
    public void addCommand(Command command) {
        this.commands.add(command);
    }
    
    public void addCommands(Command... commands) {
        this.commands.addAll(List.of(commands));
    }
    
    public Iterator<Command> getByName(String name) {
        return this.commands.stream()
                .filter(a -> a.name().equalsIgnoreCase(name))
                .iterator();
    }
    
}

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

package fr.asynconf.bot;

import fr.asynconf.bot.database.DBConnector;
import fr.asynconf.bot.events.MessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.simpleyaml.configuration.file.YamlConfiguration;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {
    
    private File configFile;
    
    public static void main(String[] args) throws LoginException, IOException, InterruptedException, SQLException, ClassNotFoundException {
        new Main().run();
    }
    
    private JDA client;
    
    private YamlConfiguration config;
    private DBConnector dbConnector;
    
    public JDA getClient() {
        return client;
    }
    
    public File getConfigFile() {
        return configFile;
    }
    
    public YamlConfiguration getConfig() {
        return config;
    }
    
    public DBConnector getDbConnector() {
        return dbConnector;
    }
    
    private void run() throws LoginException, IOException, InterruptedException, SQLException, ClassNotFoundException {
        
        this.dbConnector = new DBConnector();
        
        File f = this.configFile = new File("config.yml");
        
        if (!f.exists()) {
            f.createNewFile();
        }
        
        this.config = YamlConfiguration.loadConfiguration(f);
        
        if (!this.config.isString("bot.token")
                || Objects.equals(this.config.getString("bot.token"), "")) {
            System.err.println("Cannot find the token in the configuration file...\n Aborting");
            this.config.set("bot.token", "");
            this.config.set("bot.guild.id", 0);
            this.config.set("bot.guild.log_channel.id", 0);
            this.config.set("bot.guild.submissions.deposit_id", 0);
            this.config.set("bot.guild.submissions.review_id", 0);
            this.config.set("bot.guild.submissions.state", "closed");
            
            this.config.save(f);
            return;
        }
        
        this.client = JDABuilder.createDefault(this.config.getString("bot.token"))
                .build();
        this.client.getPresence().setActivity(Activity.playing("attendre l'Asynconf"));
        
        this.client.awaitReady();
        
        this.registerSlashCommands();
        
        this.registerEvents();
    }
    
    private void registerSlashCommands() {
        
        Guild asynconfGuild = this.client.getGuildById(this.config.getLong("bot.guild.id"));
        if (asynconfGuild == null) {
            System.err.println("The bot isn't in the right guild !");
            this.client.shutdownNow();
            System.exit(1);
        }
        
        List<String> commands = asynconfGuild.retrieveCommands().complete()
                .stream()
                .map(a -> a.getName())
                .collect(Collectors.toList());
        
        if (!commands.contains("tournoi")) {
            asynconfGuild
                    .upsertCommand("tournoi", "MOD | Commandes en rapport avec le tournoi")
                    .addSubcommands(
                            new SubcommandData("start", "MOD | Démarre le tournoi")
                    )
                    .addSubcommands(
                            new SubcommandData("end", "MOD | Termine le tournoi")
                    )
                    .queue();
        }
        
        if (!commands.contains("ban")) {
            asynconfGuild
                    .upsertCommand("ban", "MOD | Bannir un utilisateur")
                    .addOption(
                            OptionType.USER,
                            "user",
                            "Utilisateur à bannir",
                            true
                    )
                    .addOptions(
                            new OptionData(OptionType.STRING, "reason", "Raison du bannissement", true)
                                    .addChoice("Spam", "Spam")
                                    .addChoice("Propos injurieux", "Propos injurieux")
                                    .addChoice("Offense à un membre", "Offense à un membre")
                                    .addChoice("Autre (préciser)", "Autre (préciser)")
                    )
                    .addOption(
                            OptionType.STRING,
                            "other-reason",
                            "Raison du bannissement (si autre choisi)",
                            false
                    )
                    .queue();
        }
        
        if (!commands.contains("submit")) {
            asynconfGuild
                    .upsertCommand("submit", "Soumettre un projet pour le tournoi de l'Asynconf")
                    .addOption(
                            OptionType.STRING,
                            "project-url",
                            "URL du projet sur https://replit.com/",
                            true
                    )
                    .queue();
        }
        
        if (!commands.contains("project")) {
            asynconfGuild
                    .upsertCommand("project", "MOD | Gérer les soumissions des projets")
                    .addSubcommands(
                            new SubcommandData("accept", "Accepter un projet à partir de l'identifiant du projet")
                                    .addOption(OptionType.INTEGER, "id", "Identifiant du projet à valider", true)
                    )
                    .addSubcommands(
                            new SubcommandData("deny", "Refuser le projet de quelqu'un à partir de l'identifiant du projet")
                                    .addOption(OptionType.INTEGER, "id", "Identifiant du projet à refuser", true)
                                    .addOptions(
                                            new OptionData(OptionType.STRING, "reason", "Raison du refus")
                                                    .setRequired(true)
                                                    .addChoice("Lien invalide", "Lien invalide")
                                                    .addChoice("Impossible d'exécuter le programme -> Mauvais langage sur le Repl",
                                                            "Impossible d'exécuter le programme -> Mauvais langage sur le Repl")
                                                    .addChoice("Trop de soumissions (si défaillance système automatique)", "Trop de soumissions")
                                    )
                    )
                    .queue();
        }
    }
    
    private void registerEvents() {
        this.client.addEventListener(new MessageListener(this));
    }
    
}

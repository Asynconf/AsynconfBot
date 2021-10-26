package fr.asynconf.bot.commands;

import fr.asynconf.bot.Main;
import fr.asynconf.bot.commands.utils.CommandExecutor;
import fr.asynconf.bot.utils.ErrorEmbed;
import fr.asynconf.bot.utils.LogEmbed;
import fr.asynconf.bot.utils.ProjectEmbed;
import fr.asynconf.bot.utils.SuccessEmbed;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

public class SubmitCommandEx implements CommandExecutor {
    
    private final TextChannel submissionsChannel;
    private final TextChannel reviewChannel;
    private final Pattern replitPattern;
    private final Main main;
    
    public SubmitCommandEx(Main main) {
        this.main = main;
        this.replitPattern = Pattern.compile(
                "(http|https)://(replit.com|repl.it)/@.+/.+"
        );
        
        Guild guild = main.getClient().getGuildById(main.getConfig().getLong("bot.guild.id"));
        this.submissionsChannel
                = guild.getTextChannelById(main.getConfig().getLong("bot.guild.submissions.deposit_id"));
        this.reviewChannel
                = guild.getTextChannelById(main.getConfig().getLong("bot.guild.submissions.review_id"));
    }
    
    @Override
    public void run(SlashCommandEvent event) throws SQLException {
        
        if (main.getConfig().getString("bot.guild.submissions.state").equalsIgnoreCase("closed")) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Les soumissions de projet ne sont actuellement pas ouvertes !")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (event.getChannel().getIdLong() != this.submissionsChannel.getIdLong()) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Veuillez exécuter cette commande dans le salon " + this.submissionsChannel.getAsMention())
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        OptionMapping oLink = event.getOption("project-url");
        if (oLink == null) {
            event.reply("Error ! The `project-url` option is empty !")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        String projectUrl = oLink.getAsString();
        
        if (!replitPattern.matcher(projectUrl).find()) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Veuillez envoyer le lien de votre projet sur Replit. " +
                                    "Autrement, votre projet ne pourra pas être évalué")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        PreparedStatement discriminantPs = this.main.getDbConnector().getConnection()
                .prepareStatement("""
                        SELECT * FROM submissions WHERE user_id=? ORDER BY id;
                        """);
        discriminantPs.setLong(1, event.getUser().getIdLong());
        ResultSet discriminantRs = discriminantPs.executeQuery();
        
        boolean pending = false;
        String state = "";
        int id = 0;
        int size = 1;
        while (discriminantRs.next()) {
            size++;
            state = discriminantRs.getString("state");
            id = discriminantRs.getInt("id");
            if (state.equalsIgnoreCase("pending")) {
                pending = true;
            }
        }
        discriminantRs.close();
        discriminantPs.close();
        
        if (size > 3) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Vous avez déjà fait 3 soumissions ! Vous ne pouvez désormais plus en faire de nouvelle.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (pending) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Votre précédente demande n'a pas encore été traitée. Veuillez patienter avant de refaire une submission.")
                            .setFooter("ID de la demande : " + id)
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        if (state.equalsIgnoreCase("success")) {
            event.replyEmbeds(new ErrorEmbed()
                            .setDescription("Votre projet a déjà été accepté. Vous ne pouvez plus soumettre de projet désormais.")
                            .setFooter("ID de la demande : " + id)
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        
        PreparedStatement insertPs = this.main.getDbConnector().getConnection()
                .prepareStatement("""
                        INSERT INTO submissions(user_id, repl_link) VALUES (?, ?);
                        """, Statement.RETURN_GENERATED_KEYS);
        insertPs.setLong(1, event.getUser().getIdLong());
        insertPs.setString(2, projectUrl);
        
        insertPs.execute();
        
        ResultSet insertedRs = insertPs.getGeneratedKeys();
        
        int insertedId = 0;
        String insertedState = "";
        if (insertedRs.next()) {
            insertedId = insertedRs.getInt(1);
            var insertedPs = this.main.getDbConnector().getConnection()
                    .prepareStatement("SELECT state FROM submissions WHERE id=?");
            insertedPs.setInt(1, insertedId);
            ResultSet insertededRs = insertedPs.executeQuery();
            if (insertededRs.next()) {
                insertedState = insertededRs.getString("state");
            }
        }
    
        int finalInsertedId = insertedId;
        this.reviewChannel.sendMessage(new ProjectEmbed(
                event.getUser(), projectUrl, insertedId, insertedState)
                        .build())
                .queue(a -> acceptReview(a, finalInsertedId));
        
        event.replyEmbeds(new SuccessEmbed()
                        .setDescription("Votre projet a bien été soumis !\n" +
                                "Vous pouvez désormais attendre une réponse afin de savoir si celui-ci est conforme.")
                        .setFooter("ID de la demande : " + insertedId)
                        .build())
                .setEphemeral(true)
                .queue();
        
        insertPs.close();
    }
    
    private void acceptReview(Message msg, int id) {
        try {
            PreparedStatement ps = this.main.getDbConnector().getConnection()
                    .prepareStatement("UPDATE submissions SET message_id=? WHERE id=?");
            ps.setLong(1, msg.getIdLong());
            ps.setLong(2, id);
            
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

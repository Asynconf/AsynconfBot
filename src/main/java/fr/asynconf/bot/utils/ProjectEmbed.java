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
        }
    }
}

package fr.asynconf.bot.commands.utils;

public record Command(String name, CommandExecutor executor) {
}

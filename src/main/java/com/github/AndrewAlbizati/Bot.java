package com.github.AndrewAlbizati;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;

public class Bot {
    private final String token;
    private DiscordApi api;

    public Bot(String token) {
        this.token = token;
    }

    /**
     * Starts the bot, loads games, adds commands, and initializes event listeners.
     */
    public void start() {
        // Create the bot
        api = new DiscordApiBuilder().setToken(token).login().join();

        // Let the user know the bot is working correctly
        System.out.println("Logged in as " + api.getYourself().getDiscriminatedName());

        // Set bot status
        api.updateStatus(UserStatus.ONLINE);
        api.updateActivity(ActivityType.PLAYING, "Type /play to start a game");

        SlashCommand.with("play", "Starts a game of Minesweeper").createGlobal(api).join();
        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            switch (event.getSlashCommandInteraction().getCommandName().toLowerCase()) {
                case "play" -> {
                    EmbedBuilder eb = new EmbedBuilder();

                    Game game = new Game(Difficulties.BEGINNER);
                    game.start();
                    game.revealAllTiles();

                    eb.setTitle("Minesweeper");
                    eb.setDescription(game.toString());

                    interaction.createImmediateResponder()
                            .addEmbed(eb)
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond().join();
                }
            }
        });
    }
}

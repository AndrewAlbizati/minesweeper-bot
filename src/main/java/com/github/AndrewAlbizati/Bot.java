package com.github.AndrewAlbizati;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class Bot {
    private final String token;
    private DiscordApi api;
    private final HashMap<Long, Game> games;

    public Bot(String token) {
        this.token = token;
        games = new HashMap<>();
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
        SlashCommand.with("flag", "Place a flag in your game",
                List.of(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "POSITION", "e.g. A5 or E6", true)
                )).createGlobal(api).join();
        SlashCommand.with("click", "Click a square in your game",
                List.of(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "POSITION", "e.g. A5 or E6", true)
                )).createGlobal(api).join();

        api.addSlashCommandCreateListener(event -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            switch (event.getSlashCommandInteraction().getCommandName().toLowerCase()) {
                case "play" -> {
                    if (games.containsKey(interaction.getUser().getId())) {
                        interaction.createImmediateResponder()
                                .setContent("Please finish your previous game before starting a new one.")
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond().join();
                        return;
                    }
                    
                    EmbedBuilder eb = new EmbedBuilder();

                    Game game = new Game();
                    games.put(interaction.getUser().getId(), game);
                    
                    game.start();

                    eb.setTitle("Minesweeper");
                    eb.setColor(Color.GRAY);
                    eb.setFooter(interaction.getUser().getDiscriminatedName(), interaction.getUser().getAvatar());
                    eb.setDescription(game.toString());

                    Message message = interaction.createImmediateResponder()
                            .addEmbed(eb)
                            .respond().join().update().join();

                    game.setMessage(message);
                    game.start();
                }

                case "flag" -> {
                    if (!games.containsKey(interaction.getUser().getId())) {
                        interaction.createImmediateResponder()
                                .setContent("You are not currently playing Minesweeper. Type /play to start a game.")
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond().join();
                        return;
                    }

                    String position = interaction.getOptionStringValueByIndex(0).orElse("");
                    if (position.length() != 2) {
                        interaction.createImmediateResponder()
                                .setContent("Invalid position. Positions are formatted as A5 or B7")
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond().join();
                        return;
                    }

                    if (!Character.isLetter(position.charAt(0)) || !Character.isDigit(position.charAt(1))) {
                        interaction.createImmediateResponder()
                                .setContent("Invalid position. Positions are formatted as A5 or B7")
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond().join();
                        return;
                    }

                    char pos1 = Character.toLowerCase(position.charAt(0));
                    int pos2 = Integer.parseInt(position.substring(1, 2));

                    int row = switch(pos1) {
                        case 'a' -> 0;
                        case 'b' -> 1;
                        case 'c' -> 2;
                        case 'd' -> 3;
                        case 'e' -> 4;
                        case 'f' -> 5;
                        case 'g' -> 6;
                        case 'h' -> 7;
                        case 'i' -> 8;
                        default -> -1;
                    };
                    int col = pos2 - 1;
                    if (col < 0 || col > 9) {
                        interaction.createImmediateResponder()
                                .setContent("Invalid position. Positions are formatted as A5 or B7")
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond().join();
                        return;
                    }

                    
                    Game game = games.get(interaction.getUser().getId());
                    game.addFlag(row, col);
                    game.refreshBoard();

                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Minesweeper");
                    eb.setDescription(game.toString());
                    eb.setFooter(interaction.getUser().getDiscriminatedName(), interaction.getUser().getAvatar());

                    game.getMessage().edit(eb).join();

                    interaction.createImmediateResponder()
                            .setContent(":thumbsup:")
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond().join();
                }

                case "click" -> {
                    if (!games.containsKey(interaction.getUser().getId())) {
                        interaction.createImmediateResponder()
                                .setContent("You are not currently playing Minesweeper. Type /play to start a game.")
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond().join();
                        return;
                    }

                    String position = interaction.getOptionStringValueByIndex(0).orElse("");
                    if (position.length() != 2) {
                        interaction.createImmediateResponder()
                                .setContent("Invalid position. Positions are formatted as A5 or B7")
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond().join();
                        return;
                    }

                    if (!Character.isLetter(position.charAt(0)) || !Character.isDigit(position.charAt(1))) {
                        interaction.createImmediateResponder()
                                .setContent("Invalid position. Positions are formatted as A5 or B7")
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond().join();
                        return;
                    }

                    char pos1 = Character.toLowerCase(position.charAt(0));
                    int pos2 = Integer.parseInt(position.substring(1, 2));

                    int row = switch(pos1) {
                        case 'a' -> 0;
                        case 'b' -> 1;
                        case 'c' -> 2;
                        case 'd' -> 3;
                        case 'e' -> 4;
                        case 'f' -> 5;
                        case 'g' -> 6;
                        case 'h' -> 7;
                        case 'i' -> 8;
                        default -> -1;
                    };
                    int col = pos2 - 1;
                    if (col < 0 || col > 9) {
                        interaction.createImmediateResponder()
                                .setContent("Invalid position. Positions are formatted as A5 or B7")
                                .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                                .respond().join();
                        return;
                    }

                    Game game = games.get(interaction.getUser().getId());
                    game.onClick(row, col);

                    EmbedBuilder eb = new EmbedBuilder();
                    if (game.hasEnded()) {
                        games.remove(interaction.getUser().getId());
                        eb.addField("You lose!", "Time: " + formatTime(System.currentTimeMillis() - game.getStartTime()));
                        eb.setColor(Color.RED);
                    } else if (game.hasWin()) {
                        games.remove(interaction.getUser().getId());
                        eb.addField("You win!", "Time: " + formatTime(System.currentTimeMillis() - game.getStartTime()));
                        eb.setColor(Color.GREEN);
                    } else {
                        game.refreshBoard();
                        eb.setColor(Color.GRAY);
                    }

                    eb.setTitle("Minesweeper");
                    eb.setDescription(game.toString());
                    eb.setFooter(interaction.getUser().getDiscriminatedName(), interaction.getUser().getAvatar());
                    game.getMessage().edit(eb).join();

                    interaction.createImmediateResponder()
                            .setContent(":thumbsup:")
                            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
                            .respond().join();
                }
            }
        });
    }

    private static String formatTime(long duration) {
        StringBuilder sb = new StringBuilder();

        long second = (duration / 1000) % 60;
        long minute = (duration / (1000 * 60)) % 60;
        long hour = (duration / (1000 * 60 * 60)) % 24;

        // Hours
        if (hour > 0) {
            sb.append(hour);
            sb.append(hour == 1 ? " hour" : " hours");

            if (minute > 0 || second > 0) {
                sb.append(", ");
            }
        }

        // Minutes
        if (minute > 0) {
            sb.append(minute);
            sb.append(minute == 1 ? " minute" : " minutes");

            if (second > 0) {
                sb.append(", ");
            }
        }

        // Seconds
        if (second > 0) {
            sb.append(second);
            sb.append(second == 1 ? " second" : " seconds");
        }

        return sb.toString();
    }
}

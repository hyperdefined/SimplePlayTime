/*
 * This file is part of SimplePlayTime.
 *
 * SimplePlayTime is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SimplePlayTime is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SimplePlayTime.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.simpleplaytime.command;

import lol.hyper.simpleplaytime.SimplePlayTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayTimeCommand implements CommandExecutor {

    private final SimplePlayTime simplePlayTime;

    public PlayTimeCommand(SimplePlayTime simplePlayTime) {
        this.simplePlayTime = simplePlayTime;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("simpleplaytime.command")) {
            sender.sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
            return true;
        }

        // if there are no args, send playtime
        if (args.length == 0) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(simplePlayTime.getMessage("messages.players-only"));
                return true;
            }
            Player player = (Player) sender;
            showPlaytime(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload": {
                if (!sender.hasPermission("simpleplaytime.reload")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
                    return true;
                }

                simplePlayTime.loadConfig();
                sender.sendMessage(Component.text("Configuration reloaded!").color(NamedTextColor.GREEN));
                break;
            }
            case "reset": {
                if (!sender.hasPermission("simpleplaytime.reset")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
                    return true;
                }

                // player did not type /simpleplaytime reset player
                if (args.length < 2) {
                    sender.sendMessage(Component.text("You must specify a player to reset!").color(NamedTextColor.RED));
                    return true;
                }

                // get the player to reset
                Player playerToReset = Bukkit.getPlayerExact(args[1]);
                if (playerToReset == null) {
                    sender.sendMessage(Component.text("That player is invalid or offline!").color(NamedTextColor.RED));
                    return true;
                }
                simplePlayTime.playTimeTools.reset(playerToReset);
                sender.sendMessage(Component.text("Playtime for " + playerToReset.getName() + " has been reset!").color(NamedTextColor.GREEN));
                break;
            }
            case "add": {
                if (!sender.hasPermission("simpleplaytime.add")) {
                    sender.sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
                    return true;
                }
                // player did not type /simpleplaytime add time player
                if (args.length < 3) {
                    sender.sendMessage(Component.text("You must specify a time and player to add to!").color(NamedTextColor.RED));
                    return true;
                }
                // get the player to reset
                Player playerToAdd = Bukkit.getPlayerExact(args[2]);
                if (playerToAdd == null) {
                    sender.sendMessage(Component.text("That player is invalid or offline!").color(NamedTextColor.RED));
                    return true;
                }
                long durationToAdd = inputToSeconds(args[1]);
                if (durationToAdd == -1) {
                    sender.sendMessage(Component.text("Invalid time format!").color(NamedTextColor.RED));
                    return true;
                }
                simplePlayTime.playTimeTools.addTime(playerToAdd, durationToAdd);
                sender.sendMessage(Component.text("Playtime for " + playerToAdd.getName() + " has been added!").color(NamedTextColor.GREEN));
                break;
            }
            default: {
                sender.sendMessage(Component.text("Unknown subcommand.").color(NamedTextColor.RED));
                // maybe send usage here
                break;
            }
        }
        return true;
    }

    /**
     * Formats the config message to have the player's time.
     *
     * @param days    Days.
     * @param hours   Hours.
     * @param minutes Minutes.
     * @param seconds Seconds.
     * @return A formatted string with the data replaced.
     */
    private Component formatTime(long days, long hours, long minutes, long seconds) {
        String message = simplePlayTime.config.getString("messages.playtime-command");
        if (message == null) {
            return Component.text("Missing message 'messages.playtime-command'").color(NamedTextColor.RED);
        }
        if (message.contains("%days%")) {
            message = message.replace("%days%", String.valueOf(days));
        }
        if (message.contains("%hours%")) {
            message = message.replace("%hours%", String.valueOf(hours));
        }
        if (message.contains("%minutes%")) {
            message = message.replace("%minutes%", String.valueOf(minutes));
        }
        if (message.contains("%seconds%")) {
            message = message.replace("%seconds%", String.valueOf(seconds));
        }
        return simplePlayTime.textUtils.format(message);
    }

    private void showPlaytime(Player player) {
        long playTime = simplePlayTime.playTimeTools.getPlaytime(player);
        long currentSession = simplePlayTime.playerSessions.get(player.getUniqueId());
        // make sure to account for their current session
        playTime = playTime + currentSession;
        long days = TimeUnit.SECONDS.toDays(playTime);
        long hours = (TimeUnit.SECONDS.toHours(playTime) - (days * 24L));
        long minutes = (TimeUnit.SECONDS.toMinutes(playTime) - (TimeUnit.SECONDS.toHours(playTime) * 60));
        long seconds = (TimeUnit.SECONDS.toSeconds(playTime) - (TimeUnit.SECONDS.toMinutes(playTime) * 60));
        Component message = formatTime(days, hours, minutes, seconds);
        player.sendMessage(message);
    }

    private long inputToSeconds(String input) {
        Pattern pattern = Pattern.compile("(\\d+[dhms])");
        Matcher matcher = pattern.matcher(input);

        List<String> parts = new ArrayList<>();

        while (matcher.find()) {
            parts.add(matcher.group());
        }

        if (parts.isEmpty()) {
            return -1;
        }

        long duration = 0;
        for (String part : parts) {
            int value = Integer.parseInt(part.replaceAll("[^0-9]", ""));
            char unit = part.charAt(part.length() - 1);

            duration += switch (unit) {
                case 'd' -> value * 86400;
                case 'h' -> value * 3600;
                case 'm' -> value * 60;
                case 's' -> value;
                default -> 0;
            };
        }

        return duration;
    }
}

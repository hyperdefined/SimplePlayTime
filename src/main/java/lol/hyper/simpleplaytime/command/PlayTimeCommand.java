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

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import lol.hyper.simpleplaytime.SimplePlayTime;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.TimeUnit;

public class PlayTimeCommand implements CommandExecutor {

    private final SimplePlayTime simplePlayTime;
    private final BukkitAudiences audiences;

    public PlayTimeCommand(SimplePlayTime simplePlayTime) {
        this.simplePlayTime = simplePlayTime;
        this.audiences = simplePlayTime.getAdventure();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            audiences.sender(sender).sendMessage(simplePlayTime.getMessage("messages.players-only"));
            return true;
        }

        if (!sender.hasPermission("simpleplaytime.command")) {
            audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("simpleplaytime.reload")) {
                simplePlayTime.loadConfig();
                audiences.sender(sender).sendMessage(Component.text("Configuration reloaded!").color(NamedTextColor.GREEN));
            } else {
                audiences.sender(sender).sendMessage(Component.text("You do not have permission for this command.").color(NamedTextColor.RED));
            }
            return true;
        }

        Player player = (Player) sender;
        PersistentDataContainer container = player.getPersistentDataContainer();
        Long playTime = 0L;
        if (container.has(simplePlayTime.playtimeKey, PersistentDataType.LONG)) {
            playTime = container.get(simplePlayTime.playtimeKey, PersistentDataType.LONG);
        }
        if (playTime == null) {
            simplePlayTime.logger.severe("Unable to find key for player " + player.getName() + ". This IS a bug. Player's current keys: " + container.getKeys());
            return true;
        }

        long currentSession = simplePlayTime.playerSessions.get(player.getUniqueId());
        // make sure to account for their current session
        playTime = playTime + currentSession;
        long days = TimeUnit.SECONDS.toDays(playTime);
        long hours = (TimeUnit.SECONDS.toHours(playTime) - (days * 24L));
        long minutes = (TimeUnit.SECONDS.toMinutes(playTime) - (TimeUnit.SECONDS.toHours(playTime) * 60));
        long seconds = (TimeUnit.SECONDS.toSeconds(playTime) - (TimeUnit.SECONDS.toMinutes(playTime) * 60));
        Component message = formatTime(days, hours, minutes, seconds);
        audiences.player(player).sendMessage(message);
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
        return simplePlayTime.miniMessage.deserialize(message);
    }
}

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

import org.jetbrains.annotations.NotNull;
import lol.hyper.simpleplaytime.SimplePlayTime;
import org.bukkit.ChatColor;
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

    public PlayTimeCommand(SimplePlayTime simplePlayTime) {
        this.simplePlayTime = simplePlayTime;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.RED + "This command is for players only.");
            return true;
        }
        Player player = (Player) sender;
        PersistentDataContainer container = player.getPersistentDataContainer();
        Integer playTime = 0;
        if (container.has(simplePlayTime.playtimeKey, PersistentDataType.INTEGER)) {
            playTime = container.get(simplePlayTime.playtimeKey, PersistentDataType.INTEGER);
        }
        if (playTime == null) {
            simplePlayTime.logger.severe("Unable to find key for player " + player.getName() + ". This IS a bug. Player's current keys: " + container.getKeys());
            return true;
        }
        int days = (int) TimeUnit.SECONDS.toDays(playTime);
        long hours = TimeUnit.SECONDS.toHours(playTime) - (days * 24L);
        long minutes = TimeUnit.SECONDS.toMinutes(playTime) - (TimeUnit.SECONDS.toHours(playTime) * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(playTime) - (TimeUnit.SECONDS.toMinutes(playTime) * 60);
        String message = String.format(ChatColor.GREEN + "You have played for %o days, %o hours, %o minutes, %o seconds.", days, hours, minutes, seconds);
        player.sendMessage(message);
        return true;
    }
}

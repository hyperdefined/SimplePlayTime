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

package lol.hyper.simpleplaytime.events;

import lol.hyper.simpleplaytime.PlayerCounter;
import lol.hyper.simpleplaytime.SimplePlayTime;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public class PlayerLeaveJoin implements Listener {

    private final SimplePlayTime simplePlayTime;

    public PlayerLeaveJoin(SimplePlayTime simplePlayTime) {
        this.simplePlayTime = simplePlayTime;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer container = player.getPersistentDataContainer();

        // player does not have the playtime key, give it to them
        if (!container.has(simplePlayTime.playtimeKey, PersistentDataType.INTEGER)) {
            // set their play time to zero seconds
            container.set(simplePlayTime.playtimeKey, PersistentDataType.INTEGER, 0);
            simplePlayTime.getAdventure().player(player).sendMessage(simplePlayTime.getMessage("messages.playtime-start"));
        }
        // create the task for player
        BukkitTask runnable = new PlayerCounter(player.getUniqueId(), simplePlayTime).runTaskTimer(simplePlayTime, 0, 20);
        simplePlayTime.playerRunnable.put(player.getUniqueId(), runnable);

        // store last player activity
        simplePlayTime.playerActivity.put(player.getUniqueId(), System.nanoTime());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PersistentDataContainer container = player.getPersistentDataContainer();
        Integer currentPlayTime = null;
        if (container.has(simplePlayTime.playtimeKey, PersistentDataType.INTEGER)) {
            currentPlayTime = container.get(simplePlayTime.playtimeKey, PersistentDataType.INTEGER);
        }
        // make sure the player has the key
        if (currentPlayTime == null) {
            simplePlayTime.logger.severe("Unable to find key for player " + player.getName() + ". This IS a bug. Player's current keys: " + container.getKeys());
            return;
        }
        // set the player's time recorded in their last session + their current time recorded
        int newPlayTime = simplePlayTime.playerSessions.get(player.getUniqueId()) + currentPlayTime;
        container.set(simplePlayTime.playtimeKey, PersistentDataType.INTEGER, newPlayTime);
        // stop the counter for this player
        simplePlayTime.playerRunnable.get(player.getUniqueId()).cancel();
        simplePlayTime.playerSessions.remove(player.getUniqueId());
    }
}

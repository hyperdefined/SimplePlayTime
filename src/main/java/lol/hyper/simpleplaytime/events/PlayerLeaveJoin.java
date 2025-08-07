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

public class PlayerLeaveJoin implements Listener {

    private final SimplePlayTime simplePlayTime;

    public PlayerLeaveJoin(SimplePlayTime simplePlayTime) {
        this.simplePlayTime = simplePlayTime;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (container.has(simplePlayTime.playtimeKey, PersistentDataType.INTEGER)) {
            convertDataType(player);
        }

        // player does not have the playtime key, give it to them
        if (!container.has(simplePlayTime.playtimeKey, PersistentDataType.LONG)) {
            // set their play time to zero seconds
            container.set(simplePlayTime.playtimeKey, PersistentDataType.LONG, 0L);
            player.sendMessage(simplePlayTime.getMessage("messages.playtime-start"));
        }
        // create the task for player
        PlayerCounter task = new PlayerCounter(player.getUniqueId(), simplePlayTime);
        player.getScheduler().runAtFixedRate(simplePlayTime, task, null, 1, 20);
        simplePlayTime.playerRunnable.put(player.getUniqueId(), task);

        // store last player activity
        simplePlayTime.playerActivity.put(player.getUniqueId(), System.nanoTime());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PersistentDataContainer container = player.getPersistentDataContainer();
        Long currentPlayTime = null;
        if (container.has(simplePlayTime.playtimeKey, PersistentDataType.LONG)) {
            currentPlayTime = container.get(simplePlayTime.playtimeKey, PersistentDataType.LONG);
        }
        // make sure the player has the key
        if (currentPlayTime == null) {
            simplePlayTime.logger.warn("Unable to find key for player {}. This IS a bug. Player's current keys: {}", player.getName(), container.getKeys());
            return;
        }
        // set the player's time recorded in their last session + their current time recorded
        long newPlayTime = simplePlayTime.playerSessions.get(player.getUniqueId()) + currentPlayTime;
        container.set(simplePlayTime.playtimeKey, PersistentDataType.LONG, newPlayTime);
        // stop the counter for this player
        simplePlayTime.playerRunnable.get(player.getUniqueId()).cancel();
        simplePlayTime.playerSessions.remove(player.getUniqueId());
    }

    /**
     * The plugin (as of 1.2.2) uses longs to save total seconds. However, we need to convert the old ints to longs.
     *
     * @param player The player to convert.
     */
    private void convertDataType(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        Integer oldSeconds;
        // don't need to check if the player has this, since we already did
        oldSeconds = container.get(simplePlayTime.playtimeKey, PersistentDataType.INTEGER);
        if (oldSeconds == null) {
            return;
        }
        Long newSeconds = Long.valueOf(oldSeconds);
        container.set(simplePlayTime.playtimeKey, PersistentDataType.LONG, newSeconds);

        simplePlayTime.logger.info("Converting playtime for {} from int -> long for storage.", player.getName());
    }
}

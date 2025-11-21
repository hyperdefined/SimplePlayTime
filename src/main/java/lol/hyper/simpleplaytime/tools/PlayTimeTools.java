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

package lol.hyper.simpleplaytime.tools;

import io.papermc.paper.persistence.PersistentDataContainerView;
import lol.hyper.simpleplaytime.SimplePlayTime;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.Buffer;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayTimeTools {

    private final SimplePlayTime simplePlayTime;

    public PlayTimeTools(SimplePlayTime simplePlayTime) {
        this.simplePlayTime = simplePlayTime;
    }

    /**
     * Get a player's playtime.
     *
     * @param player The player.
     * @return The time in seconds.
     */
    public long getPlaytime(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        Long currentPlayTime = null;
        if (container.has(simplePlayTime.playtimeKey, PersistentDataType.LONG)) {
            currentPlayTime = container.get(simplePlayTime.playtimeKey, PersistentDataType.LONG);
        }
        // make sure the player has the keyS
        if (currentPlayTime == null) {
            simplePlayTime.logger.warn("Unable to find key for player {}. This IS a bug. Player's current keys: {}", player.getName(), container.getKeys());
            return -1;
        }

        return currentPlayTime + getSession(player);
    }

    /**
     * Get the player's current session.
     *
     * @param player The player.
     * @return Their current session in seconds.
     */
    public long getSession(Player player) {
        return simplePlayTime.playerSessions.get(player.getUniqueId());
    }

    /**
     * Set the player's current session.
     *
     * @param player The player.
     * @param seconds The seconds to set to.
     */
    public void setSession(Player player, long seconds) {
        simplePlayTime.playerSessions.put(player.getUniqueId(), seconds);
    }

    /**
     * Finish a player's session and add their playtime.
     *
     * @param player Player to add to.
     */
    public void finishSession(Player player) {
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

        long newPlayTime = simplePlayTime.playerSessions.get(player.getUniqueId()) + currentPlayTime;
        container.set(simplePlayTime.playtimeKey, PersistentDataType.LONG, newPlayTime);

    }

    /**
     * Add playtime to a player.
     *
     * @param player    The player to add.
     * @param timeToAdd The time in seconds to add.
     */
    public void addTime(Player player, long timeToAdd) {
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

        long newPlayTime = timeToAdd + currentPlayTime;
        container.set(simplePlayTime.playtimeKey, PersistentDataType.LONG, newPlayTime);

    }

    /**
     * Reset a player's playtime to 0.
     *
     * @param player The player to reset.
     */
    public void reset(Player player) {
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

        setSession(player, 0);
        container.set(simplePlayTime.playtimeKey, PersistentDataType.LONG, 0L);
    }

    /**
     * Initialize a player and create their playtime key.
     *
     * @param player The player.
     */
    public void initPlayer(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (container.has(simplePlayTime.playtimeKey, PersistentDataType.INTEGER)) {
            convertDataType(player);
        }

        // check if they have playtime saved
        if (container.has(simplePlayTime.playtimeKey, PersistentDataType.LONG)) {
            return;
        }

        // set their initial time to 0
        container.set(simplePlayTime.playtimeKey, PersistentDataType.LONG, 0L);
        player.sendMessage(simplePlayTime.getMessage("messages.playtime-start"));
        simplePlayTime.logger.info("Starting to track playtime for {}", player.getName());
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

    /**
     * Format given seconds into a format for a message.
     *
     * @param format   The format from config to use.
     * @param playTime The playtime in seconds.
     */
    public String format(String format, Long playTime) {
        String configMessage = simplePlayTime.config.getString(format);
        if (configMessage == null) {
            simplePlayTime.logger.info("Invalid format: {}", format);
            return null;
        }

        long days = TimeUnit.SECONDS.toDays(playTime);
        long hours = (TimeUnit.SECONDS.toHours(playTime) - (days * 24L));
        long minutes = (TimeUnit.SECONDS.toMinutes(playTime) - (TimeUnit.SECONDS.toHours(playTime) * 60));
        long seconds = (TimeUnit.SECONDS.toSeconds(playTime) - (TimeUnit.SECONDS.toMinutes(playTime) * 60));

        if (configMessage.contains("%days%")) {
            configMessage = configMessage.replace("%days%", String.valueOf(days));
        }
        if (configMessage.contains("%hours%")) {
            configMessage = configMessage.replace("%hours%", String.format("%02d", hours));
        }
        if (configMessage.contains("%minutes%")) {
            configMessage = configMessage.replace("%minutes%", String.format("%02d", minutes));
        }
        if (configMessage.contains("%seconds%")) {
            configMessage = configMessage.replace("%seconds%", String.format("%02d", seconds));
        }
        return configMessage;
    }

    public Map.Entry<String, Long> getTopPlayer() {
        String topName = null;
        long topTime = 0;

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            PersistentDataContainerView container = player.getPersistentDataContainer();

            if (!container.has(simplePlayTime.playtimeKey, PersistentDataType.LONG)) {
                continue;
            }

            Long time = container.get(simplePlayTime.playtimeKey, PersistentDataType.LONG);
            if (time == null) {
                continue;
            }

            if (player.isOnline()) {
                Player onlinePlayer = Bukkit.getPlayer(player.getUniqueId());
                time = time + getSession(onlinePlayer);
            }

            if (time > topTime) {
                topTime = time;
                topName = player.getName();
            }
        }

        return topName == null ? null : Map.entry(topName, topTime);
    }
}

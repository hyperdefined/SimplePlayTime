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

package lol.hyper.simpleplaytime;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerCounter extends BukkitRunnable {

    final Player player;
    final PersistentDataContainer container;
    final SimplePlayTime simplePlayTime;

    public PlayerCounter(Player player, SimplePlayTime simplePlayTime) {
        this.player = player;
        this.simplePlayTime = simplePlayTime;
        container = player.getPersistentDataContainer();
    }

    @Override
    public void run() {
        // get the current time
        long currentTime = System.nanoTime();
        // see how much time has past since last player interaction
        long lastInteraction = (currentTime - simplePlayTime.playerActivity.get(player)) / 1000000000;
        // if it's shorter than the afk timeout, count the playtime
        if (lastInteraction <= simplePlayTime.config.getInt("afk-timeout")) {
            Integer playTime = 0;
            // get the current playtime
            if (container.has(simplePlayTime.playtimeKey, PersistentDataType.INTEGER)) {
                playTime = container.get(simplePlayTime.playtimeKey, PersistentDataType.INTEGER);
            }
            if (playTime == null) {
                simplePlayTime.logger.severe("Unable to find key for player " + player.getName() + ". This IS a bug. Player's current keys: " + container.getKeys());
                this.cancel();
                return;
            }
            // add 1 second to playtime
            container.set(simplePlayTime.playtimeKey, PersistentDataType.INTEGER, playTime + 1);
        } else {
            player.sendMessage("lol");
        }
    }
}

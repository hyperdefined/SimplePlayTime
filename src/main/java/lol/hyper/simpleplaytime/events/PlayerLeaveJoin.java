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

import lol.hyper.simpleplaytime.timers.PlayerCounter;
import lol.hyper.simpleplaytime.SimplePlayTime;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveJoin implements Listener {

    private final SimplePlayTime simplePlayTime;

    public PlayerLeaveJoin(SimplePlayTime simplePlayTime) {
        this.simplePlayTime = simplePlayTime;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        simplePlayTime.playTimeTools.initPlayer(player);

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

        simplePlayTime.playTimeTools.finishSession(player);
        simplePlayTime.playerRunnable.get(player.getUniqueId()).cancel();
        simplePlayTime.playerSessions.remove(player.getUniqueId());
    }
}

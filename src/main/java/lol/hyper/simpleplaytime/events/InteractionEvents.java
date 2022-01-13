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

import lol.hyper.simpleplaytime.SimplePlayTime;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class InteractionEvents implements Listener {

    private final SimplePlayTime simplePlayTime;

    public InteractionEvents(SimplePlayTime simplePlayTime) {
        this.simplePlayTime = simplePlayTime;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            // store last player activity
            simplePlayTime.playerActivity.put(player, System.nanoTime());
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        // store last player activity
        simplePlayTime.playerActivity.put(player, System.nanoTime());
    }
}

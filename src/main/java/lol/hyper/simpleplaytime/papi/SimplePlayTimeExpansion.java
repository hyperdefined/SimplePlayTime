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

package lol.hyper.simpleplaytime.papi;


import lol.hyper.simpleplaytime.SimplePlayTime;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SimplePlayTimeExpansion extends PlaceholderExpansion {

    private final SimplePlayTime simplePlayTime;

    public SimplePlayTimeExpansion(SimplePlayTime simplePlayTime) {
        this.simplePlayTime = simplePlayTime;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "simpleplaytime";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", simplePlayTime.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return simplePlayTime.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        // ignore null players
        if (player == null) {
            return null;
        }

        long time = simplePlayTime.playTimeTools.getPlaytime(player);
        long session = simplePlayTime.playTimeTools.getSession(player);

        switch (params.toLowerCase(Locale.ROOT)) {
            case "playtime" -> {
                return simplePlayTime.playTimeTools.format("format.short", time);
            }
            case "session" -> {
                return simplePlayTime.playTimeTools.format("format.session", session);
            }
            case "top_name" -> {
                return simplePlayTime.playTimeTools.getTopPlayer().getKey();
            }
            case "top_time" -> {
                long topTime = simplePlayTime.playTimeTools.getTopPlayer().getValue();
                return simplePlayTime.playTimeTools.format("format.short", topTime);
            }
        }

        return null;
    }
}

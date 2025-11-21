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

import lol.hyper.hyperlib.HyperLib;
import lol.hyper.hyperlib.bstats.HyperStats;
import lol.hyper.hyperlib.releases.HyperUpdater;
import lol.hyper.hyperlib.utils.TextUtils;
import lol.hyper.simpleplaytime.command.PlayTimeCommand;
import lol.hyper.simpleplaytime.events.InteractionEvents;
import lol.hyper.simpleplaytime.events.PlayerLeaveJoin;
import lol.hyper.simpleplaytime.papi.SimplePlayTimeExpansion;
import lol.hyper.simpleplaytime.timers.PlayerCounter;
import lol.hyper.simpleplaytime.tools.PlayTimeTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public final class SimplePlayTime extends JavaPlugin {

    public final HashMap<UUID, PlayerCounter> playerRunnable = new HashMap<>();
    public final HashMap<UUID, Long> playerActivity = new HashMap<>();
    public final HashMap<UUID, Long> playerSessions = new HashMap<>();
    public final ComponentLogger logger = this.getComponentLogger();
    public final File configFile = new File(this.getDataFolder(), "config.yml");
    public PlayTimeTools playTimeTools;
    public FileConfiguration config;
    public final NamespacedKey playtimeKey = new NamespacedKey(this, "playtime");

    public HyperLib hyperLib;
    public TextUtils textUtils;

    @Override
    public void onEnable() {
        hyperLib = new HyperLib(this);
        hyperLib.setup();

        HyperStats stats = new HyperStats(hyperLib, 13941);
        stats.setup();

        textUtils = new TextUtils(hyperLib);
        playTimeTools = new PlayTimeTools(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            logger.info("PlaceholderAPI is detected! Enabling support.");
            SimplePlayTimeExpansion expansion = new SimplePlayTimeExpansion(this);
            if (expansion.register()) {
                logger.info("Successfully registered placeholders!");
            } else {
                logger.warn("Unable to register placeholders!");
            }
        }

        loadConfig();
        Bukkit.getPluginManager().registerEvents(new InteractionEvents(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLeaveJoin(this), this);

        this.getCommand("playtime").setExecutor(new PlayTimeCommand(this));

        HyperUpdater updater = new HyperUpdater(hyperLib);
        updater.setGitHub("hyperdefined", "SimplePlayTime");
        updater.setModrinth("Z84sevGO");
        updater.setHangar("SimplePlayTime", "paper");
        updater.check();
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        int CONFIG_VERSION = 2;
        if (config.getInt("config-version") != CONFIG_VERSION) {
            logger.warn("You configuration is out of date! Some features may not work!");
        }
    }

    /**
     * Gets a message from messages.yml.
     *
     * @param path The path to the message.
     * @return Component with formatting applied.
     */
    public Component getMessage(String path) {
        String message = config.getString(path);
        if (message == null) {
            logger.warn("{} is not a valid message!", path);
            return Component.text("Invalid path! " + path).color(NamedTextColor.RED);
        }
        return textUtils.format(message);
    }
}

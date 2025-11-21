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

package lol.hyper.simpleplaytime.timers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lol.hyper.simpleplaytime.SimplePlayTime;

import java.util.UUID;
import java.util.function.Consumer;

public class PlayerCounter implements Consumer<ScheduledTask> {

    private final UUID player;
    private final SimplePlayTime simplePlayTime;
    private ScheduledTask task;

    public PlayerCounter(UUID player, SimplePlayTime simplePlayTime) {
        this.player = player;
        this.simplePlayTime = simplePlayTime;
        // start the session with 0 seconds
        simplePlayTime.playerSessions.put(player, 0L);
    }

    @Override
    public void accept(ScheduledTask task) {
        this.task = task;
        // get the current time
        long currentTime = System.nanoTime();
        // see how much time has past since last player interaction
        long lastInteraction = (currentTime - simplePlayTime.playerActivity.get(player)) / 1000000000;
        // if it's shorter than the afk timeout, count the playtime
        if (lastInteraction <= simplePlayTime.config.getInt("afk-timeout")) {
            simplePlayTime.playerSessions.compute(player, (k, currentSeconds) -> currentSeconds + 1);
        }
    }

    public void cancel() {
        if (task != null) {
            this.task.cancel();
        }
    }
}

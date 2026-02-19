package xyz.lokili.loutils.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utility class for unified scheduler access across Folia's region-based threading
 */
public final class SchedulerUtil {
    
    private SchedulerUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Run task on global region scheduler (for non-world-specific tasks)
     */
    public static void runGlobal(Plugin plugin, Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, (t) -> task.run());
    }
    
    /**
     * Run delayed task on global region scheduler
     */
    public static void runGlobalDelayed(Plugin plugin, Runnable task, long delayTicks) {
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (t) -> task.run(), delayTicks);
    }
    
    /**
     * Run repeating task on global region scheduler
     */
    public static ScheduledTask runGlobalTimer(Plugin plugin, Consumer<ScheduledTask> task, long delayTicks, long periodTicks) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task, delayTicks, periodTicks);
    }
    
    /**
     * Run task on region scheduler for specific location
     */
    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        Bukkit.getRegionScheduler().run(plugin, location, (t) -> task.run());
    }
    
    /**
     * Run delayed task on region scheduler for specific location
     */
    public static void runAtLocationDelayed(Plugin plugin, Location location, Runnable task, long delayTicks) {
        Bukkit.getRegionScheduler().runDelayed(plugin, location, (t) -> task.run(), delayTicks);
    }
    
    /**
     * Run task on entity scheduler
     */
    public static void runForEntity(Plugin plugin, Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, (t) -> task.run(), null);
    }
    
    /**
     * Run delayed task on entity scheduler
     */
    public static void runForEntityDelayed(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        entity.getScheduler().runDelayed(plugin, (t) -> task.run(), null, delayTicks);
    }
    
    /**
     * Run repeating task on entity scheduler
     */
    public static ScheduledTask runForEntityTimer(Plugin plugin, Entity entity, Consumer<ScheduledTask> task, long delayTicks, long periodTicks) {
        return entity.getScheduler().runAtFixedRate(plugin, task, null, delayTicks, periodTicks);
    }
    
    /**
     * Run async task
     */
    public static void runAsync(Plugin plugin, Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, (t) -> task.run());
    }
    
    /**
     * Run delayed async task
     */
    public static void runAsyncDelayed(Plugin plugin, Runnable task, long delay, TimeUnit unit) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, (t) -> task.run(), delay, unit);
    }
    
    /**
     * Run repeating async task
     */
    public static ScheduledTask runAsyncTimer(Plugin plugin, Consumer<ScheduledTask> task, long delay, long period, TimeUnit unit) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task, delay, period, unit);
    }
    
    /**
     * Broadcast message to all players (uses global scheduler)
     */
    public static void broadcast(Plugin plugin, String message) {
        runGlobal(plugin, () -> Bukkit.broadcast(ColorUtil.colorize(message)));
    }
}

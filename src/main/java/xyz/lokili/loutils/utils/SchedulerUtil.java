package xyz.lokili.loutils.utils;

import dev.lolib.scheduler.Scheduler;
import dev.lolib.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utility class for unified scheduler access across Folia's region-based threading
 * Now uses LoLib Scheduler under the hood
 * 
 * @deprecated Use {@link Scheduler} from LoLib directly
 */
@Deprecated
public final class SchedulerUtil {
    
    private SchedulerUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Run task on global region scheduler (for non-world-specific tasks)
     */
    public static void runGlobal(Plugin plugin, Runnable task) {
        Scheduler.get(plugin).run(task);
    }
    
    /**
     * Run delayed task on global region scheduler
     */
    public static void runGlobalDelayed(Plugin plugin, Runnable task, long delayTicks) {
        Scheduler.get(plugin).runLater(task, delayTicks);
    }
    
    /**
     * Run repeating task on global region scheduler
     */
    public static ScheduledTask runGlobalTimer(Plugin plugin, Consumer<ScheduledTask> task, long delayTicks, long periodTicks) {
        // LoLib Scheduler doesn't support Consumer<ScheduledTask>, need to adapt
        return Scheduler.get(plugin).runTimer(() -> task.accept(null), delayTicks, periodTicks);
    }
    
    /**
     * Run task on region scheduler for specific location
     */
    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        Scheduler.get(plugin).runAtLocation(location, task);
    }
    
    /**
     * Run delayed task on region scheduler for specific location
     */
    public static void runAtLocationDelayed(Plugin plugin, Location location, Runnable task, long delayTicks) {
        Scheduler.get(plugin).runLaterAtLocation(location, task, delayTicks);
    }
    
    /**
     * Run task on entity scheduler
     */
    public static void runForEntity(Plugin plugin, Entity entity, Runnable task) {
        Scheduler.get(plugin).runAtEntity(entity, task);
    }
    
    /**
     * Run delayed task on entity scheduler
     */
    public static void runForEntityDelayed(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        Scheduler.get(plugin).runLaterAtEntity(entity, task, delayTicks);
    }
    
    /**
     * Run repeating task on entity scheduler
     */
    public static ScheduledTask runForEntityTimer(Plugin plugin, Entity entity, Consumer<ScheduledTask> task, long delayTicks, long periodTicks) {
        return Scheduler.get(plugin).runTimerAtEntity(entity, () -> task.accept(null), delayTicks, periodTicks);
    }
    
    /**
     * Run async task
     */
    public static void runAsync(Plugin plugin, Runnable task) {
        Scheduler.get(plugin).runAsync(task);
    }
    
    /**
     * Run delayed async task
     */
    public static void runAsyncDelayed(Plugin plugin, Runnable task, long delay, TimeUnit unit) {
        long ticks = unit.toMillis(delay) / 50; // Convert to ticks (50ms per tick)
        Scheduler.get(plugin).runLaterAsync(task, ticks);
    }
    
    /**
     * Run repeating async task
     */
    public static ScheduledTask runAsyncTimer(Plugin plugin, Consumer<ScheduledTask> task, long delay, long period, TimeUnit unit) {
        long delayTicks = unit.toMillis(delay) / 50;
        long periodTicks = unit.toMillis(period) / 50;
        return Scheduler.get(plugin).runTimerAsync(() -> task.accept(null), delayTicks, periodTicks);
    }
    
    /**
     * Broadcast message to all players (uses global scheduler)
     */
    public static void broadcast(Plugin plugin, String message) {
        runGlobal(plugin, () -> Bukkit.broadcast(ColorUtil.colorize(message)));
    }
}

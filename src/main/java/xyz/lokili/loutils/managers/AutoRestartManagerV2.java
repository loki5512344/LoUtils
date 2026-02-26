package xyz.lokili.loutils.managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IAutoRestartManager;
import xyz.lokili.loutils.managers.restart.RestartExecutor;
import xyz.lokili.loutils.managers.restart.RestartScheduler;
import xyz.lokili.loutils.managers.restart.WarningBroadcaster;

import java.util.concurrent.TimeUnit;

/**
 * Улучшенный менеджер авто-рестарта
 * Применяет Single Responsibility Principle - делегирует задачи компонентам
 */
public class AutoRestartManagerV2 implements IAutoRestartManager {
    
    private final LoUtils plugin;
    private final RestartScheduler scheduler;
    private final WarningBroadcaster broadcaster;
    private final RestartExecutor executor;
    
    private ScheduledTask timerTask;
    private volatile boolean running;
    
    public AutoRestartManagerV2(LoUtils plugin) {
        this.plugin = plugin;
        this.scheduler = new RestartScheduler();
        this.broadcaster = new WarningBroadcaster(plugin);
        this.executor = new RestartExecutor(plugin);
        this.running = false;
    }
    
    @Override
    public void start() {
        if (running) return;
        
        if (!plugin.getConfigManager().getAutoRestartConfig().getBoolean("enabled", false)) {
            return;
        }
        
        // Сброс предупреждений и расчет времени
        broadcaster.reset();
        scheduler.calculateNextRestart(plugin.getConfigManager().getAutoRestartConfig());
        
        // Запуск таймера
        startTimer();
        running = true;
        
        plugin.getLogger().info("AutoRestart timer started. Restart in " + scheduler.getFormattedRemaining());
    }
    
    @Override
    public void stop() {
        if (!running) return;
        
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        running = false;
        plugin.getLogger().info("AutoRestart timer stopped.");
    }
    
    @Override
    public void reload() {
        stop();
        if (plugin.getConfigManager().getAutoRestartConfig().getBoolean("enabled", false)) {
            start();
        }
    }
    
    /**
     * Запуск таймера проверки
     */
    private void startTimer() {
        timerTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {
            checkAndProcess();
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    /**
     * Проверка и обработка рестарта
     */
    private void checkAndProcess() {
        if (scheduler.isTimeToRestart()) {
            stop();
            executor.executeRestart();
            return;
        }
        
        long remaining = scheduler.getRemainingMillis();
        broadcaster.checkAndBroadcast(remaining, plugin.getConfigManager().getAutoRestartConfig());
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public String getTimeRemaining() {
        if (!running) return "N/A";
        return scheduler.getFormattedRemaining();
    }
    
    @Override
    public long[] getTimeRemainingParts() {
        if (!running) return new long[]{0, 0, 0};
        return scheduler.getRemainingParts();
    }
}

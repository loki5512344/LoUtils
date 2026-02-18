package xyz.lokili.loutils.managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IAutoRestartManager;
import xyz.lokili.loutils.utils.ColorUtil;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AutoRestartManager implements IAutoRestartManager {
    
    private final LoUtils plugin;
    private ScheduledTask timerTask;
    private long restartTimeMillis;
    private boolean running;
    
    public AutoRestartManager(LoUtils plugin) {
        this.plugin = plugin;
        this.running = false;
    }
    
    @Override
    public void start() {
        if (running) return;
        
        if (!plugin.getConfigManager().getAutoRestartConfig().getBoolean("enabled", false)) {
            return;
        }
        
        calculateRestartTime();
        startTimer();
        running = true;
        plugin.getLogger().info("AutoRestart timer started. Restart in " + getTimeRemaining());
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
    
    private void calculateRestartTime() {
        String dailyTime = plugin.getConfigManager().getAutoRestartConfig().getString("daily_time", "");
        
        if (dailyTime != null && !dailyTime.isEmpty()) {
            // Ежедневный рестарт в определённое время
            try {
                LocalTime targetTime = LocalTime.parse(dailyTime, DateTimeFormatter.ofPattern("HH:mm"));
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime nextRestart = now.with(targetTime);
                
                if (nextRestart.isBefore(now) || nextRestart.isEqual(now)) {
                    nextRestart = nextRestart.plusDays(1);
                }
                
                long millisUntilRestart = ChronoUnit.MILLIS.between(now, nextRestart);
                restartTimeMillis = System.currentTimeMillis() + millisUntilRestart;
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid daily_time format. Using interval instead.");
                useIntervalTime();
            }
        } else {
            useIntervalTime();
        }
    }
    
    private void useIntervalTime() {
        int intervalMinutes = plugin.getConfigManager().getAutoRestartConfig().getInt("interval_minutes", 360);
        restartTimeMillis = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L);
    }
    
    private void startTimer() {
        // Используем AsyncScheduler для таймера (не зависит от региона)
        timerTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {
            checkAndWarn();
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    private void checkAndWarn() {
        long remaining = restartTimeMillis - System.currentTimeMillis();
        
        if (remaining <= 0) {
            executeRestart();
            return;
        }
        
        long remainingMinutes = remaining / 60000;
        long remainingSeconds = (remaining / 1000) % 60;
        
        // Проверяем предупреждения
        List<Integer> warnings = plugin.getConfigManager().getAutoRestartConfig().getIntegerList("warnings");
        
        for (int warningMinute : warnings) {
            // Предупреждение в начале минуты
            if (remainingMinutes == warningMinute && remainingSeconds == 0) {
                broadcastWarning(warningMinute);
                break;
            }
        }
        
        // Последние 10 секунд
        if (remainingMinutes == 0 && remainingSeconds <= 10 && remainingSeconds > 0) {
            broadcastSecondsWarning((int) remainingSeconds);
        }
    }
    
    private void broadcastWarning(int minutes) {
        String message = plugin.getConfigManager().getPrefix() +
                plugin.getConfigManager().getMessage("autorestart.warning")
                        .replace("{time}", String.valueOf(minutes));
        
        // Используем GlobalRegionScheduler для broadcast
        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            Bukkit.broadcast(ColorUtil.colorize(message));
        });
    }
    
    private void broadcastSecondsWarning(int seconds) {
        String message = plugin.getConfigManager().getPrefix() +
                plugin.getConfigManager().getMessage("autorestart.warning-seconds")
                        .replace("{time}", String.valueOf(seconds));
        
        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            Bukkit.broadcast(ColorUtil.colorize(message));
        });
    }
    
    private void executeRestart() {
        stop();
        
        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            // Финальное сообщение
            String message = plugin.getConfigManager().getPrefix() +
                    plugin.getConfigManager().getMessage("autorestart.now");
            Bukkit.broadcast(ColorUtil.colorize(message));
            
            // Сохранение миров
            if (plugin.getConfigManager().getAutoRestartConfig().getBoolean("save_before_restart", true)) {
                for (World world : Bukkit.getWorlds()) {
                    world.save();
                }
                plugin.getLogger().info("All worlds saved before restart.");
            }
            
            // Рестарт через 3 секунды
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, (task) -> {
                Bukkit.shutdown();
            }, 60L); // 3 секунды
        });
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public String getTimeRemaining() {
        if (!running) return "N/A";
        
        long remaining = restartTimeMillis - System.currentTimeMillis();
        if (remaining <= 0) return "0";
        
        long hours = remaining / 3600000;
        long minutes = (remaining % 3600000) / 60000;
        long seconds = (remaining % 60000) / 1000;
        
        return String.format("%dч %dм %dс", hours, minutes, seconds);
    }
    
    @Override
    public long[] getTimeRemainingParts() {
        if (!running) return new long[]{0, 0, 0};
        
        long remaining = restartTimeMillis - System.currentTimeMillis();
        if (remaining <= 0) return new long[]{0, 0, 0};
        
        long hours = remaining / 3600000;
        long minutes = (remaining % 3600000) / 60000;
        long seconds = (remaining % 60000) / 1000;
        
        return new long[]{hours, minutes, seconds};
    }
}

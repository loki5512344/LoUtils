package xyz.lokili.loutils.managers.restart;

import dev.lolib.utils.TimeFormatter;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.SchedulerUtil;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Отвечает за отправку предупреждений о рестарте
 * Single Responsibility: Warning broadcasts
 */
public class WarningBroadcaster {
    
    private final LoUtils plugin;
    private final Set<String> sentWarnings;
    
    public WarningBroadcaster(LoUtils plugin) {
        this.plugin = plugin;
        this.sentWarnings = new HashSet<>();
    }
    
    /**
     * Проверяет и отправляет предупреждения если нужно
     */
    public void checkAndBroadcast(long remainingMillis, FileConfiguration config) {
        long remainingMinutes = remainingMillis / 60000;
        long remainingSeconds = (remainingMillis / 1000) % 60;
        
        // Предупреждения по минутам
        List<Integer> warnings = config.getIntegerList("warnings");
        for (int warningMinute : warnings) {
            if (remainingMinutes == warningMinute && remainingSeconds >= 0 && remainingSeconds <= 1) {
                String key = "minute_" + warningMinute;
                if (!sentWarnings.contains(key)) {
                    broadcastMinuteWarning(warningMinute);
                    sentWarnings.add(key);
                }
                break;
            }
        }
        
        // Последние 10 секунд
        if (remainingMinutes == 0 && remainingSeconds <= 10 && remainingSeconds > 0) {
            int seconds = (int) remainingSeconds;
            String key = "second_" + seconds;
            if (!sentWarnings.contains(key)) {
                broadcastSecondWarning(seconds);
                sentWarnings.add(key);
            }
        }
    }
    
    /**
     * Отправка предупреждения за N минут
     */
    private void broadcastMinuteWarning(int minutes) {
        // Используем TimeFormatter из LoLib для красивого форматирования
        Duration duration = Duration.ofMinutes(minutes);
        String timeFormatted = TimeFormatter.formatSmart(duration);
        
        String message = plugin.getContainer().getConfigManager().getPrefix() +
                plugin.getContainer().getConfigManager().getMessage("autorestart.warning")
                        .replace("{time}", timeFormatted);
        
        SchedulerUtil.broadcast(plugin, message);
    }
    
    /**
     * Отправка предупреждения за N секунд
     */
    private void broadcastSecondWarning(int seconds) {
        // Используем TimeFormatter из LoLib
        Duration duration = Duration.ofSeconds(seconds);
        String timeFormatted = TimeFormatter.formatSmart(duration);
        
        String message = plugin.getContainer().getConfigManager().getPrefix() +
                plugin.getContainer().getConfigManager().getMessage("autorestart.warning-seconds")
                        .replace("{time}", timeFormatted);
        
        SchedulerUtil.broadcast(plugin, message);
    }
    
    /**
     * Сброс отправленных предупреждений (для нового цикла)
     */
    public void reset() {
        sentWarnings.clear();
    }
}

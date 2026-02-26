package xyz.lokili.loutils.managers.restart;

import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Отвечает за расчет времени следующего рестарта
 * Single Responsibility: Scheduling logic
 */
public class RestartScheduler {
    
    private long restartTimeMillis;
    
    /**
     * Рассчитывает время следующего рестарта на основе конфига
     */
    public void calculateNextRestart(FileConfiguration config) {
        String dailyTime = config.getString("daily_time", "");
        
        if (dailyTime != null && !dailyTime.isEmpty()) {
            calculateDailyRestart(dailyTime);
        } else {
            calculateIntervalRestart(config.getInt("interval_minutes", 360));
        }
    }
    
    /**
     * Рестарт в определенное время каждый день
     */
    private void calculateDailyRestart(String timeString) {
        try {
            LocalTime targetTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextRestart = now.with(targetTime);
            
            if (nextRestart.isBefore(now) || nextRestart.isEqual(now)) {
                nextRestart = nextRestart.plusDays(1);
            }
            
            long millisUntilRestart = ChronoUnit.MILLIS.between(now, nextRestart);
            restartTimeMillis = System.currentTimeMillis() + millisUntilRestart;
        } catch (Exception e) {
            // Fallback to interval
            calculateIntervalRestart(360);
        }
    }
    
    /**
     * Рестарт через определенный интервал
     */
    private void calculateIntervalRestart(int intervalMinutes) {
        restartTimeMillis = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L);
    }
    
    /**
     * Получить оставшееся время в миллисекундах
     */
    public long getRemainingMillis() {
        return restartTimeMillis - System.currentTimeMillis();
    }
    
    /**
     * Получить оставшееся время в частях (часы, минуты, секунды)
     */
    public long[] getRemainingParts() {
        long remaining = getRemainingMillis();
        if (remaining <= 0) return new long[]{0, 0, 0};
        
        long hours = remaining / 3600000;
        long minutes = (remaining % 3600000) / 60000;
        long seconds = (remaining % 60000) / 1000;
        
        return new long[]{hours, minutes, seconds};
    }
    
    /**
     * Проверка что время рестарта наступило
     */
    public boolean isTimeToRestart() {
        return getRemainingMillis() <= 0;
    }
    
    /**
     * Форматированная строка оставшегося времени
     */
    public String getFormattedRemaining() {
        long[] parts = getRemainingParts();
        return String.format("%dч %dм %dс", parts[0], parts[1], parts[2]);
    }
}

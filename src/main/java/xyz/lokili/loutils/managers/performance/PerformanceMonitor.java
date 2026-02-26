package xyz.lokili.loutils.managers.performance;

import org.bukkit.Bukkit;

/**
 * Мониторинг производительности сервера
 * Single Responsibility: Performance metrics collection
 */
public class PerformanceMonitor {
    
    private final double tpsThreshold;
    private final long cooldownMillis;
    private long lastReportTime = 0;
    
    public PerformanceMonitor(double tpsThreshold, long cooldownSeconds) {
        this.tpsThreshold = tpsThreshold;
        this.cooldownMillis = cooldownSeconds * 1000L;
    }
    
    /**
     * Проверяет нужно ли генерировать отчет
     */
    public boolean shouldGenerateReport() {
        double currentTPS = Bukkit.getTPS()[0];
        
        // TPS в норме
        if (currentTPS >= tpsThreshold) {
            return false;
        }
        
        // Проверка cooldown
        long now = System.currentTimeMillis();
        if (now - lastReportTime < cooldownMillis) {
            return false;
        }
        
        lastReportTime = now;
        return true;
    }
    
    /**
     * Получить текущие метрики производительности
     */
    public PerformanceMetrics getCurrentMetrics() {
        return new PerformanceMetrics(
            Bukkit.getTPS()[0],
            Bukkit.getAverageTickTime(),
            tpsThreshold
        );
    }
    
    /**
     * Класс для хранения метрик
     */
    public static class PerformanceMetrics {
        private final double tps;
        private final double mspt;
        private final double threshold;
        
        public PerformanceMetrics(double tps, double mspt, double threshold) {
            this.tps = tps;
            this.mspt = mspt;
            this.threshold = threshold;
        }
        
        public double getTps() {
            return tps;
        }
        
        public double getMspt() {
            return mspt;
        }
        
        public double getThreshold() {
            return threshold;
        }
    }
}

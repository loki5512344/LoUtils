package xyz.lokili.loutils.managers;

import dev.lolib.performance.TPSMonitor;
import dev.lolib.scheduler.ScheduledTask;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.managers.performance.ReportGenerator;
import xyz.lokili.loutils.managers.performance.WebhookSender;

/**
 * Профайлер производительности с использованием TPSMonitor из LoLib
 * Упрощенная версия - делегирует мониторинг TPS библиотеке
 */
public class PerformanceProfiler {
    
    private final LoUtils plugin;
    private final TPSMonitor tpsMonitor; // Может быть null на Folia
    private final ReportGenerator reportGenerator;
    private final WebhookSender webhookSender;
    
    private ScheduledTask monitorTask;
    private long lastReportTime = 0;
    
    public PerformanceProfiler(LoUtils plugin, TPSMonitor tpsMonitor) {
        this.plugin = plugin;
        this.tpsMonitor = tpsMonitor;
        this.reportGenerator = new ReportGenerator();
        this.webhookSender = new WebhookSender(plugin);
    }
    
    /**
     * Запуск мониторинга
     */
    public void start() {
        if (tpsMonitor == null) {
            plugin.loLogger().warn("Performance Profiler disabled - TPSMonitor not available (Folia compatibility)");
            return;
        }
        
        var config = getConfig();
        if (!config.getBoolean("enabled", true)) return;
        
        double threshold = config.getDouble("tps-threshold", 15.0);
        int interval = config.getInt("check-interval", 30);
        
        // Используем TPSMonitor listener для автоматического мониторинга
        tpsMonitor.addListener(threshold, currentTps -> {
            checkPerformance(currentTps, threshold);
        });
        
        plugin.loLogger().info("Performance Profiler started (threshold: " + threshold + " TPS, check interval: " + interval + "s)");
    }
    
    /**
     * Остановка мониторинга
     */
    public void stop() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }
    
    /**
     * Проверка производительности
     */
    private void checkPerformance(double currentTps, double threshold) {
        var config = getConfig();
        long cooldownMillis = config.getLong("report-cooldown", 300) * 1000L;
        
        // Проверка cooldown
        long now = System.currentTimeMillis();
        if (now - lastReportTime < cooldownMillis) {
            return;
        }
        
        lastReportTime = now;
        
        // Генерация и отправка отчета
        var metrics = new PerformanceMetrics(currentTps, tpsMonitor.getTickTime(), threshold);
        String report = reportGenerator.generateReport(metrics, config);
        
        String webhookUrl = config.getString("webhook-url", "");
        webhookSender.sendAsync(report, webhookUrl);
    }
    
    /**
     * Получение конфига
     */
    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getConfig(ConfigConstants.PERFORMANCE_CONFIG);
    }
    
    /**
     * Класс для хранения метрик (упрощенная версия)
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

package xyz.lokili.loutils.managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.managers.performance.PerformanceMonitor;
import xyz.lokili.loutils.managers.performance.ReportGenerator;
import xyz.lokili.loutils.managers.performance.WebhookSender;

import java.util.concurrent.TimeUnit;

/**
 * Улучшенный профайлер производительности
 * Применяет Single Responsibility Principle - делегирует задачи компонентам
 */
public class PerformanceProfilerV2 {
    
    private final LoUtils plugin;
    private final PerformanceMonitor monitor;
    private final ReportGenerator reportGenerator;
    private final WebhookSender webhookSender;
    
    private ScheduledTask monitorTask;
    
    public PerformanceProfilerV2(LoUtils plugin) {
        this.plugin = plugin;
        
        var config = getConfig();
        this.monitor = new PerformanceMonitor(
            config.getDouble("tps-threshold", 15.0),
            config.getLong("report-cooldown", 300)
        );
        this.reportGenerator = new ReportGenerator();
        this.webhookSender = new WebhookSender(plugin);
    }
    
    /**
     * Запуск мониторинга
     */
    public void start() {
        var config = getConfig();
        if (!config.getBoolean("enabled", true)) return;
        
        int interval = config.getInt("check-interval", 30);
        
        monitorTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {
            checkPerformance();
        }, interval, interval, TimeUnit.SECONDS);
        
        plugin.getLogger().info("Performance Profiler started (check interval: " + interval + "s)");
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
    private void checkPerformance() {
        if (!monitor.shouldGenerateReport()) {
            return;
        }
        
        // Генерация и отправка отчета
        var metrics = monitor.getCurrentMetrics();
        String report = reportGenerator.generateReport(metrics, getConfig());
        
        String webhookUrl = getConfig().getString("webhook-url", "");
        webhookSender.sendAsync(report, webhookUrl);
    }
    
    /**
     * Получение конфига
     */
    private FileConfiguration getConfig() {
        return plugin.getConfigManager().getConfig(ConfigConstants.PERFORMANCE_CONFIG);
    }
}

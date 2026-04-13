package xyz.lokili.loutils.managers.restart;

import org.bukkit.Bukkit;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.SchedulerUtil;

/**
 * Отвечает за выполнение рестарта сервера
 * Single Responsibility: Restart execution
 */
public class RestartExecutor {
    
    private final LoUtils plugin;
    
    public RestartExecutor(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Выполняет рестарт сервера
     */
    public void executeRestart() {
        SchedulerUtil.runGlobal(plugin, () -> {
            // Финальное сообщение
            String message = plugin.getContainer().getConfigManager().getPrefix() +
                    plugin.getContainer().getConfigManager().getMessage("autorestart.now");
            SchedulerUtil.broadcast(plugin, message);
            
            // Логирование
            if (plugin.getContainer().getConfigManager().getAutoRestartConfig().getBoolean("save_before_restart", true)) {
                plugin.getLogger().info("Worlds will be saved automatically during shutdown.");
            }
            
            // Рестарт через 3 секунды
            SchedulerUtil.runGlobalDelayed(plugin, Bukkit::shutdown, 60L);
        });
    }
}

package xyz.lokili.loutils.listeners.base;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IConfigManager;

/**
 * Базовый класс для всех листенеров
 * Устраняет дублирование проверки модулей и получения конфигов
 */
public abstract class BaseListener implements Listener {
    
    protected final LoUtils plugin;
    protected final IConfigManager configManager;
    protected final FileConfiguration config;
    private final String moduleName;
    
    /**
     * @param plugin Экземпляр плагина
     * @param configManager Менеджер конфигураций
     * @param moduleName Имя модуля для проверки (null если всегда включен)
     * @param configPath Путь к конфигу модуля (null если не нужен)
     */
    protected BaseListener(LoUtils plugin, IConfigManager configManager, String moduleName, String configPath) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.moduleName = moduleName;
        this.config = configPath != null ? configManager.getConfig(configPath) : null;
    }
    
    /**
     * Проверяет, включен ли модуль
     */
    protected boolean isModuleEnabled() {
        return moduleName == null || configManager.isModuleEnabled(moduleName);
    }
    
    /**
     * Быстрая проверка для использования в начале обработчиков событий
     * @return true если модуль включен, false если нужно прервать обработку
     */
    protected boolean checkEnabled() {
        // Проверяем включенность модуля в основном конфиге
        if (!isModuleEnabled()) {
            return false;
        }
        
        // Проверяем enabled в конфиге самого модуля (если конфиг есть)
        if (config != null && !config.getBoolean("enabled", true)) {
            return false;
        }
        
        return true;
    }
}

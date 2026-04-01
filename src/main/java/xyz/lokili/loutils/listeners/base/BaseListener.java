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
    private final String moduleName;
    /** Путь к YAML модуля в data folder (null если конфиг не используется). */
    private final String moduleConfigPath;

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
        this.moduleConfigPath = configPath;
    }

    /**
     * Актуальный конфиг модуля. После {@code /loutils reload} всегда отражает файл на диске,
     * в отличие от устаревшего снимка {@code FileConfiguration} в поле.
     */
    protected FileConfiguration moduleConfig() {
        return moduleConfigPath != null ? configManager.getConfig(moduleConfigPath) : null;
    }

    /**
     * Проверяет, включен ли модуль
     */
    protected boolean isModuleEnabled() {
        return moduleName == null || configManager.isModuleEnabled(moduleName);
    }

    /**
     * Быстрая проверка для использования в начале обработчиков событий
     *
     * @return true если модуль включен, false если нужно прервать обработку
     */
    protected boolean checkEnabled() {
        if (!isModuleEnabled()) {
            return false;
        }

        FileConfiguration c = moduleConfig();
        if (c != null && !c.getBoolean("enabled", true)) {
            return false;
        }

        if (c == null && moduleName != null) {
            return false;
        }

        return true;
    }
}

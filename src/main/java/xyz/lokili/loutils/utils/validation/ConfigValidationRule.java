package xyz.lokili.loutils.utils.validation;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Интерфейс для правил валидации конфигов
 */
@FunctionalInterface
public interface ConfigValidationRule {
    
    /**
     * Валидирует конфиг и возвращает результат
     * 
     * @param config конфиг для валидации
     * @return результат валидации
     */
    ValidationResult validate(FileConfiguration config);
}

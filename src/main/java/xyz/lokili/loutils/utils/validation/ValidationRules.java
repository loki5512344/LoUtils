package xyz.lokili.loutils.utils.validation;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.function.Predicate;

/**
 * Фабрика для создания правил валидации
 * Применяет DRY принцип - переиспользуемые правила
 */
public class ValidationRules {
    
    /**
     * Проверка что значение существует
     */
    public static ConfigValidationRule required(String path, String errorMessage) {
        return config -> {
            if (!config.contains(path)) {
                return ValidationResult.error(errorMessage);
            }
            return ValidationResult.success();
        };
    }
    
    /**
     * Проверка целого числа в диапазоне
     */
    public static ConfigValidationRule intRange(String path, int min, int max, int defaultValue) {
        return config -> {
            var builder = ValidationResult.builder();
            
            if (!config.contains(path)) {
                builder.warning(path + " not found")
                       .fix(path, defaultValue, "Using default value: " + defaultValue);
                return builder.build();
            }
            
            int value = config.getInt(path, defaultValue);
            if (value < min || value > max) {
                builder.warning(path + " must be " + min + "-" + max + ", got: " + value)
                       .fix(path, defaultValue, "Resetting to default: " + defaultValue);
            }
            
            return builder.build();
        };
    }
    
    /**
     * Проверка дробного числа в диапазоне
     */
    public static ConfigValidationRule doubleRange(String path, double min, double max, double defaultValue) {
        return config -> {
            var builder = ValidationResult.builder();
            
            if (!config.contains(path)) {
                builder.warning(path + " not found")
                       .fix(path, defaultValue, "Using default value: " + defaultValue);
                return builder.build();
            }
            
            double value = config.getDouble(path, defaultValue);
            if (value < min || value > max) {
                builder.warning(path + " must be " + min + "-" + max + ", got: " + value)
                       .fix(path, defaultValue, "Resetting to default: " + defaultValue);
            }
            
            return builder.build();
        };
    }
    
    /**
     * Проверка что список не пустой
     */
    public static ConfigValidationRule listNotEmpty(String path, String warningMessage) {
        return config -> {
            var builder = ValidationResult.builder();
            
            if (!config.contains(path)) {
                builder.warning(path + " not found");
                return builder.build();
            }
            
            List<?> list = config.getList(path);
            if (list == null || list.isEmpty()) {
                builder.warning(warningMessage);
            }
            
            return builder.build();
        };
    }
    
    /**
     * Проверка строки по условию
     */
    public static ConfigValidationRule stringMatch(String path, Predicate<String> condition, 
                                                   String errorMessage, String defaultValue) {
        return config -> {
            var builder = ValidationResult.builder();
            
            String value = config.getString(path, defaultValue);
            if (value == null || !condition.test(value)) {
                builder.warning(errorMessage)
                       .fix(path, defaultValue, "Using default value");
            }
            
            return builder.build();
        };
    }
    
    /**
     * Проверка что URL не пустой (для webhook)
     */
    public static ConfigValidationRule webhookUrl(String path, boolean required) {
        return config -> {
            var builder = ValidationResult.builder();
            
            String url = config.getString(path, "");
            if (url.isEmpty()) {
                if (required) {
                    builder.error(path + " is required but empty");
                } else {
                    builder.warning(path + " is empty - feature will be disabled");
                }
            } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                builder.warning(path + " should start with http:// or https://");
            }
            
            return builder.build();
        };
    }
    
    /**
     * Проверка времени в формате HH:mm
     */
    public static ConfigValidationRule timeFormat(String path, boolean required) {
        return config -> {
            var builder = ValidationResult.builder();
            
            String time = config.getString(path, "");
            if (time.isEmpty()) {
                if (required) {
                    builder.error(path + " is required but empty");
                }
                return builder.build();
            }
            
            if (!time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                builder.warning(path + " has invalid format. Expected HH:mm, got: " + time)
                       .fix(path, "", "Clearing invalid time format");
            }
            
            return builder.build();
        };
    }
    
    /**
     * Комбинация нескольких правил
     */
    public static ConfigValidationRule combine(ConfigValidationRule... rules) {
        return config -> {
            var builder = ValidationResult.builder();
            
            for (ConfigValidationRule rule : rules) {
                ValidationResult result = rule.validate(config);
                
                if (!result.isValid()) {
                    builder.valid(false);
                }
                
                result.getErrors().forEach(builder::error);
                result.getWarnings().forEach(builder::warning);
                result.getFixes().forEach(builder::fix);
            }
            
            return builder.build();
        };
    }
}

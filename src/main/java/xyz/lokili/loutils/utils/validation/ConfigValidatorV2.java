package xyz.lokili.loutils.utils.validation;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Улучшенный валидатор конфигов
 * Применяет принципы SOLID и DRY
 */
public class ConfigValidatorV2 {
    
    @SuppressWarnings("unused") // Может использоваться в будущем
    private final Plugin plugin;
    private final Logger logger;
    private final Map<String, ConfigValidationRule> validators;
    
    public ConfigValidatorV2(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.validators = new HashMap<>();
        
        registerValidators();
    }
    
    /**
     * Регистрация всех валидаторов
     */
    private void registerValidators() {
        // AutoRestart
        validators.put("conf/autorestart.yml", ValidationRules.combine(
            ValidationRules.intRange("interval_minutes", 1, 10080, 360),
            ValidationRules.timeFormat("daily_time", false),
            ValidationRules.listNotEmpty("warnings", "AutoRestart: warnings list is empty")
        ));
        
        // Performance
        validators.put("conf/performance.yml", ValidationRules.combine(
            ValidationRules.doubleRange("tps-threshold", 1.0, 20.0, 15.0),
            ValidationRules.intRange("check-interval", 10, 300, 30),
            ValidationRules.intRange("report-cooldown", 60, 3600, 300),
            ValidationRules.intRange("max-entities-in-report", 5, 100, 20),
            ValidationRules.intRange("max-players-in-report", 5, 50, 10),
            ValidationRules.intRange("entity-per-chunk-threshold", 10, 500, 50),
            ValidationRules.webhookUrl("webhook-url", false)
        ));
        
        // Enchant
        validators.put("conf/enchant.yml", ValidationRules.combine(
            ValidationRules.intRange("max_level", 0, 32767, 0)
        ));
        
        // SleepPercentage
        validators.put("conf/sleeppercentage.yml", ValidationRules.combine(
            ValidationRules.intRange("sleep-percentage", 1, 100, 30)
        ));
        
        // FastLeafDecay
        validators.put("conf/fastleafdecay.yml", ValidationRules.combine(
            ValidationRules.intRange("decay-delay", 1, 200, 40),
            ValidationRules.intRange("search-radius", 1, 20, 5)
        ));
        
        // Cauldron
        validators.put("conf/cauldron.yml", ValidationRules.combine(
            ValidationRules.intRange("concrete-cleaning.water-cost", 1, 3, 1),
            ValidationRules.intRange("washing.water-cost", 1, 3, 1)
        ));
        
        // VillagerLeash
        validators.put("conf/villagerleash.yml", ValidationRules.combine(
            ValidationRules.intRange("emerald-attraction.attraction-radius", 1, 32, 8),
            ValidationRules.doubleRange("emerald-attraction.movement-speed", 0.1, 2.0, 0.6)
        ));
        
        // CustomWorldHeight
        validators.put("conf/customworldheight.yml", ValidationRules.combine(
            ValidationRules.intRange("min-y", -2032, 0, -64),
            ValidationRules.intRange("max-y", 256, 2032, 320)
        ));
        
        // TPSBar
        validators.put("conf/tpsbar.yml", ValidationRules.combine(
            ValidationRules.intRange("update-interval", 1, 100, 20)
        ));
    }
    
    /**
     * Валидация всех конфигов
     */
    public void validateAll(Map<String, FileConfiguration> configs) {
        logger.info("Starting configuration validation...");
        
        int totalErrors = 0;
        int totalWarnings = 0;
        int totalFixes = 0;
        
        for (Map.Entry<String, ConfigValidationRule> entry : validators.entrySet()) {
            String configPath = entry.getKey();
            ConfigValidationRule validator = entry.getValue();
            
            FileConfiguration config = configs.get(configPath);
            if (config == null) {
                logger.warning("Config not loaded: " + configPath);
                continue;
            }
            
            ValidationResult result = validator.validate(config);
            
            // Логирование ошибок
            for (String error : result.getErrors()) {
                logger.severe("[" + configPath + "] ERROR: " + error);
                totalErrors++;
            }
            
            // Логирование предупреждений
            for (String warning : result.getWarnings()) {
                logger.warning("[" + configPath + "] " + warning);
                totalWarnings++;
            }
            
            // Применение исправлений
            for (ValidationResult.ConfigFix fix : result.getFixes()) {
                config.set(fix.getPath(), fix.getValue());
                logger.info("[" + configPath + "] Fixed: " + fix.getReason());
                totalFixes++;
            }
        }
        
        // Итоговый отчет
        if (totalErrors > 0) {
            logger.severe("Validation completed with " + totalErrors + " errors!");
        } else if (totalWarnings > 0 || totalFixes > 0) {
            logger.info("Validation completed: " + totalWarnings + " warnings, " + totalFixes + " fixes applied");
        } else {
            logger.info("All configurations are valid!");
        }
    }
    
    /**
     * Валидация одного конфига
     */
    public ValidationResult validate(String configPath, FileConfiguration config) {
        ConfigValidationRule validator = validators.get(configPath);
        if (validator == null) {
            return ValidationResult.success();
        }
        
        return validator.validate(config);
    }
}

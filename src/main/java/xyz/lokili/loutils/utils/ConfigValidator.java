package xyz.lokili.loutils.utils;

import xyz.lokili.loutils.LoUtils;

import java.util.List;

public class ConfigValidator {
    
    private final LoUtils plugin;
    
    public ConfigValidator(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    public void validateAll() {
        validateAutoRestart();
        validateSleepPercentage();
        validateFastLeafDecay();
        validateCauldron();
        validateVillagerLeash();
    }
    
    private void validateAutoRestart() {
        var config = plugin.getConfigManager().getAutoRestartConfig();
        
        if (!config.getBoolean("enabled", false)) return;
        
        int interval = config.getInt("interval_minutes", 360);
        if (interval < 1) {
            plugin.getLogger().warning("AutoRestart: interval_minutes must be >= 1, using default 360");
            config.set("interval_minutes", 360);
        }
        
        List<Integer> warnings = config.getIntegerList("warnings");
        if (warnings.isEmpty()) {
            plugin.getLogger().warning("AutoRestart: warnings list is empty, using defaults");
        }
    }
    
    private void validateSleepPercentage() {
        var config = plugin.getConfigManager().getConfig("conf/sleeppercentage.yml");
        
        if (!config.getBoolean("enabled", true)) return;
        
        int percentage = config.getInt("sleep-percentage", 30);
        if (percentage < 1 || percentage > 100) {
            plugin.getLogger().warning("SleepPercentage: sleep-percentage must be 1-100, using default 30");
            config.set("sleep-percentage", 30);
        }
    }
    
    private void validateFastLeafDecay() {
        var config = plugin.getConfigManager().getConfig("conf/fastleafdecay.yml");
        
        if (!config.getBoolean("enabled", true)) return;
        
        int delay = config.getInt("decay-delay", 40);
        if (delay < 1) {
            plugin.getLogger().warning("FastLeafDecay: decay-delay must be >= 1, using default 40");
            config.set("decay-delay", 40);
        }
        
        int radius = config.getInt("search-radius", 5);
        if (radius < 1 || radius > 20) {
            plugin.getLogger().warning("FastLeafDecay: search-radius must be 1-20, using default 5");
            config.set("search-radius", 5);
        }
    }
    
    private void validateCauldron() {
        var config = plugin.getConfigManager().getConfig("conf/cauldron.yml");
        
        if (!config.getBoolean("enabled", true)) return;
        
        int concreteWater = config.getInt("concrete-cleaning.water-cost", 1);
        if (concreteWater < 1 || concreteWater > 3) {
            plugin.getLogger().warning("Cauldron: concrete-cleaning.water-cost must be 1-3, using default 1");
            config.set("concrete-cleaning.water-cost", 1);
        }
        
        int washingWater = config.getInt("washing.water-cost", 1);
        if (washingWater < 1 || washingWater > 3) {
            plugin.getLogger().warning("Cauldron: washing.water-cost must be 1-3, using default 1");
            config.set("washing.water-cost", 1);
        }
    }
    
    private void validateVillagerLeash() {
        var config = plugin.getConfigManager().getConfig("conf/villagerleash.yml");
        
        if (!config.getBoolean("enabled", true)) return;
        
        int radius = config.getInt("emerald-attraction.attraction-radius", 8);
        if (radius < 1 || radius > 32) {
            plugin.getLogger().warning("VillagerLeash: attraction-radius must be 1-32, using default 8");
            config.set("emerald-attraction.attraction-radius", 8);
        }
        
        double speed = config.getDouble("emerald-attraction.movement-speed", 0.6);
        if (speed < 0.1 || speed > 2.0) {
            plugin.getLogger().warning("VillagerLeash: movement-speed must be 0.1-2.0, using default 0.6");
            config.set("emerald-attraction.movement-speed", 0.6);
        }
    }
}

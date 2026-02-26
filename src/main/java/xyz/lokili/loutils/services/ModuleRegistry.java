package xyz.lokili.loutils.services;

import org.bukkit.plugin.Plugin;

/**
 * Handles module enable/disable state
 * Single Responsibility: Module management
 */
public class ModuleRegistry {
    
    private final Plugin plugin;
    
    public ModuleRegistry(Plugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean isModuleEnabled(String module) {
        return plugin.getConfig().getBoolean("modules." + module, true);
    }
    
    public void setModuleEnabled(String module, boolean enabled) {
        plugin.getConfig().set("modules." + module, enabled);
        plugin.saveConfig();
    }
}

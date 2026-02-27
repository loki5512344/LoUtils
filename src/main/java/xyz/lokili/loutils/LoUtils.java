package xyz.lokili.loutils;

import org.bukkit.Bukkit;
import dev.lolib.core.LoPlugin;
import xyz.lokili.loutils.api.*;
import xyz.lokili.loutils.listeners.InvSeeListener;
import xyz.lokili.loutils.managers.*;
import xyz.lokili.loutils.placeholders.LoUtilsExpansion;
import xyz.lokili.loutils.registry.CommandRegistry;
import xyz.lokili.loutils.registry.ListenerRegistry;
import xyz.lokili.loutils.utils.MessageUtil;

public class LoUtils extends LoPlugin {
    
    private IConfigManager configManager;
    private IWhitelistManager whitelistManager;
    private IAutoRestartManager autoRestartManager;
    private ITPSBarManager tpsBarManager;
    private IWorldLockManager worldLockManager;
    private ICustomWorldHeightManager customWorldHeightManager;
    private PerformanceProfiler performanceProfiler;
    private MessageUtil messageUtil;
    private ListenerRegistry listenerRegistry;
    
    @Override
    protected void enable() {
        // Config manager first (validation happens inside)
        configManager = new ConfigManager(this);
        
        // Initialize utilities
        messageUtil = new MessageUtil(this);
        
        // Initialize managers
        whitelistManager = new WhitelistManager(this);
        autoRestartManager = new AutoRestartManager(this);
        tpsBarManager = new TPSBarManager(this);
        worldLockManager = new WorldLockManager(this);
        customWorldHeightManager = new CustomWorldHeightManager(this);
        performanceProfiler = new PerformanceProfiler(this);
        
        // Register commands and listeners
        new CommandRegistry(this).registerAll();
        listenerRegistry = new ListenerRegistry(this);
        listenerRegistry.registerAll();
        
        // Start autorestart if enabled
        if (configManager.isModuleEnabled("autorestart")) {
            autoRestartManager.start();
        }
        
        // Start performance profiler if enabled
        if (configManager.isModuleEnabled("performance")) {
            performanceProfiler.start();
        }
        
        // Register PlaceholderAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LoUtilsExpansion(this).register();
            loLogger().info("PlaceholderAPI expansion registered!");
        }
        
        loLogger().info("LoUtils v2.5.0 enabled!");
    }
    
    @Override
    protected void disable() {
        if (listenerRegistry != null && listenerRegistry.getInvSeeListener() != null) {
            listenerRegistry.getInvSeeListener().shutdown();
        }
        if (tpsBarManager != null) tpsBarManager.shutdown();
        if (autoRestartManager != null) autoRestartManager.stop();
        if (performanceProfiler != null) performanceProfiler.stop();
        if (whitelistManager != null) whitelistManager.saveWhitelist();
        
        loLogger().info("LoUtils v2.5.0 disabled!");
    }
    
    public void reload() {
        configManager.reloadAll();
        whitelistManager.reload();
        autoRestartManager.reload();
    }
    
    // Getters
    public IConfigManager getConfigManager() { return configManager; }
    public IWhitelistManager getWhitelistManager() { return whitelistManager; }
    public IAutoRestartManager getAutoRestartManager() { return autoRestartManager; }
    public ITPSBarManager getTPSBarManager() { return tpsBarManager; }
    public IWorldLockManager getWorldLockManager() { return worldLockManager; }
    public ICustomWorldHeightManager getCustomWorldHeightManager() { return customWorldHeightManager; }
    public MessageUtil getMessageUtil() { return messageUtil; }
    public InvSeeListener getInvSeeListener() { 
        return listenerRegistry != null ? listenerRegistry.getInvSeeListener() : null; 
    }
}

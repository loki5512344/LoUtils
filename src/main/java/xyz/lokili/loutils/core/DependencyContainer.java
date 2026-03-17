package xyz.lokili.loutils.core;

import dev.lolib.performance.AsyncExecutor;
import dev.lolib.performance.TPSMonitor;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.*;
import xyz.lokili.loutils.factories.ItemFactory;
import xyz.lokili.loutils.managers.*;
import xyz.lokili.loutils.managers.pose.PoseManager;
import xyz.lokili.loutils.services.EffectService;
import xyz.lokili.loutils.utils.MessageUtil;

/**
 * Контейнер зависимостей для LoUtils
 * Централизует создание и управление всеми менеджерами
 * Применяет Dependency Injection принцип
 */
public class DependencyContainer {
    
    private final LoUtils plugin;
    
    // Core
    private final IConfigManager configManager;
    private final MessageUtil messageUtil;
    
    // Services
    private final ItemFactory itemFactory;
    private final EffectService effectService;
    
    // Managers
    private final IWhitelistManager whitelistManager;
    private final IAutoRestartManager autoRestartManager;
    private final ITPSBarManager tpsBarManager;
    private final IWorldLockManager worldLockManager;
    private final ICustomWorldHeightManager customWorldHeightManager;
    private final PoseManager poseManager;
    
    // Performance
    private final PerformanceProfiler performanceProfiler;
    private final TPSMonitor tpsMonitor; // Может быть null на Folia
    private final AsyncExecutor asyncExecutor;
    
    public DependencyContainer(LoUtils plugin) {
        this.plugin = plugin;
        
        // Core - создаём первыми
        this.configManager = new ConfigManager(plugin);
        this.messageUtil = new MessageUtil(plugin);
        
        // Services - создаём до менеджеров
        this.itemFactory = new ItemFactory(plugin);
        this.effectService = new EffectService();
        
        // Performance - создаём до менеджеров
        // TPSMonitor не работает на Folia (использует старый Bukkit Scheduler)
        TPSMonitor monitor = null;
        try {
            monitor = TPSMonitor.get(plugin);
        } catch (UnsupportedOperationException e) {
            plugin.loLogger().warn("TPSMonitor disabled - not compatible with Folia");
        }
        this.tpsMonitor = monitor;
        
        this.asyncExecutor = AsyncExecutor.builder(plugin)
            .corePoolSize(2)
            .maxPoolSize(8)
            .build();
        
        // Managers - создаём с зависимостями
        this.whitelistManager = new WhitelistManager(plugin, configManager);
        this.autoRestartManager = new AutoRestartManager(plugin, configManager);
        this.tpsBarManager = new TPSBarManager(plugin, tpsMonitor);
        this.worldLockManager = new WorldLockManager(plugin, configManager);
        this.customWorldHeightManager = new CustomWorldHeightManager(plugin, configManager);
        this.performanceProfiler = new PerformanceProfiler(plugin, tpsMonitor);
        this.poseManager = new PoseManager(plugin);
    }
    
    /**
     * Запускает сервисы которые требуют автозапуска
     */
    public void startServices() {
        // AutoRestart
        if (configManager.isModuleEnabled("autorestart")) {
            autoRestartManager.start();
        }
        
        // Performance Profiler
        if (configManager.isModuleEnabled("performance")) {
            performanceProfiler.start();
        }
    }
    
    /**
     * Останавливает все сервисы
     */
    public void shutdown() {
        // Останавливаем менеджеры
        if (autoRestartManager != null) {
            autoRestartManager.stop();
        }
        
        if (performanceProfiler != null) {
            performanceProfiler.stop();
        }
        
        if (tpsBarManager != null) {
            tpsBarManager.shutdown();
        }
        
        if (poseManager != null) {
            poseManager.clearAll();
        }
        
        // Сохраняем данные
        if (whitelistManager != null) {
            whitelistManager.saveWhitelist();
        }
        
        // Останавливаем async executor
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }
    }
    
    /**
     * Перезагружает конфигурации
     */
    public void reload() {
        configManager.reloadAll();
        whitelistManager.reload();
        autoRestartManager.reload();
    }
    
    // Getters
    public IConfigManager getConfigManager() { return configManager; }
    public MessageUtil getMessageUtil() { return messageUtil; }
    public ItemFactory getItemFactory() { return itemFactory; }
    public EffectService getEffectService() { return effectService; }
    public IWhitelistManager getWhitelistManager() { return whitelistManager; }
    public IAutoRestartManager getAutoRestartManager() { return autoRestartManager; }
    public ITPSBarManager getTPSBarManager() { return tpsBarManager; }
    public IWorldLockManager getWorldLockManager() { return worldLockManager; }
    public ICustomWorldHeightManager getCustomWorldHeightManager() { return customWorldHeightManager; }
    public PerformanceProfiler getPerformanceProfiler() { return performanceProfiler; }
    public TPSMonitor getTPSMonitor() { return tpsMonitor; }
    public AsyncExecutor getAsyncExecutor() { return asyncExecutor; }
    public PoseManager getPoseManager() { return poseManager; }
}

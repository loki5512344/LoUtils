package xyz.lokili.loutils;

import org.bukkit.Bukkit;
import dev.lolib.core.LoPlugin;
import xyz.lokili.loutils.core.DependencyContainer;
import xyz.lokili.loutils.placeholders.LoUtilsExpansion;
import xyz.lokili.loutils.registry.CommandRegistry;
import xyz.lokili.loutils.registry.ListenerRegistry;

public class LoUtils extends LoPlugin {
    
    private DependencyContainer container;
    private ListenerRegistry listenerRegistry;
    
    @Override
    protected void enable() {
        // Создаём контейнер зависимостей
        container = new DependencyContainer(this);
        
        // Регистрируем команды и листенеры
        new CommandRegistry(this).registerAll();
        listenerRegistry = new ListenerRegistry(this, container.getConfigManager());
        listenerRegistry.registerAll();
        
        // Запускаем сервисы
        container.startServices();
        
        // Регистрируем PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LoUtilsExpansion(this).register();
            loLogger().info("PlaceholderAPI expansion registered!");
        }
        
        loLogger().info("LoUtils v2.5.0 enabled!");
    }
    
    @Override
    protected void disable() {
        // Останавливаем InvSeeListener
        if (listenerRegistry != null && listenerRegistry.getInvSeeListener() != null) {
            listenerRegistry.getInvSeeListener().shutdown();
        }
        
        // Останавливаем все сервисы
        if (container != null) {
            container.shutdown();
        }
        
        loLogger().info("LoUtils v2.5.0 disabled!");
    }
    
    public void reload() {
        if (container != null) {
            container.reload();
        }
    }
    
    // Главный геттер - доступ к контейнеру
    public DependencyContainer getContainer() {
        return container;
    }
    
    // Alias для getContainer
    public DependencyContainer getDependencies() {
        return container;
    }
    
    // Удобные геттеры для обратной совместимости
    public xyz.lokili.loutils.api.IConfigManager getConfigManager() {
        return container.getConfigManager();
    }
    
    public xyz.lokili.loutils.api.IWhitelistManager getWhitelistManager() {
        return container.getWhitelistManager();
    }
    
    public xyz.lokili.loutils.api.IAutoRestartManager getAutoRestartManager() {
        return container.getAutoRestartManager();
    }
    
    public xyz.lokili.loutils.api.ITPSBarManager getTPSBarManager() {
        return container.getTPSBarManager();
    }
    
    public xyz.lokili.loutils.api.IWorldLockManager getWorldLockManager() {
        return container.getWorldLockManager();
    }
    
    public xyz.lokili.loutils.api.ICustomWorldHeightManager getCustomWorldHeightManager() {
        return container.getCustomWorldHeightManager();
    }
    
    public xyz.lokili.loutils.utils.MessageUtil getMessageUtil() {
        return container.getMessageUtil();
    }
    
    public xyz.lokili.loutils.listeners.player.InvSeeListener getInvSeeListener() {
        return listenerRegistry != null ? listenerRegistry.getInvSeeListener() : null;
    }
}

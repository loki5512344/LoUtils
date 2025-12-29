package xyz.lokili.loutils;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.lokili.loutils.commands.AutoRestartCommand;
import xyz.lokili.loutils.commands.DimensionLockCommand;
import xyz.lokili.loutils.commands.WhitelistCommand;
import xyz.lokili.loutils.listeners.PlayerJoinListener;
import xyz.lokili.loutils.listeners.PortalListener;
import xyz.lokili.loutils.managers.AutoRestartManager;
import xyz.lokili.loutils.managers.DimensionLockManager;
import xyz.lokili.loutils.managers.WhitelistManager;

public class LoUtils extends JavaPlugin {
    
    private WhitelistManager whitelistManager;
    private AutoRestartManager autoRestartManager;
    private DimensionLockManager dimensionLockManager;
    
    @Override
    public void onEnable() {
        // Сохраняем конфиг по умолчанию
        saveDefaultConfig();
        
        // Инициализируем менеджеры
        whitelistManager = new WhitelistManager(this);
        autoRestartManager = new AutoRestartManager(this);
        dimensionLockManager = new DimensionLockManager(this);
        
        // Регистрируем команды
        WhitelistCommand whitelistCommand = new WhitelistCommand(this);
        getCommand("lw").setExecutor(whitelistCommand);
        getCommand("lw").setTabCompleter(whitelistCommand);
        
        AutoRestartCommand autoRestartCommand = new AutoRestartCommand(this);
        getCommand("lar").setExecutor(autoRestartCommand);
        getCommand("lar").setTabCompleter(autoRestartCommand);
        
        DimensionLockCommand dimensionLockCommand = new DimensionLockCommand(this);
        getCommand("ll").setExecutor(dimensionLockCommand);
        getCommand("ll").setTabCompleter(dimensionLockCommand);
        
        // Регистрируем слушатели
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        
        // Запускаем авто-рестарт если включён
        autoRestartManager.start();
        
        getLogger().info("LoUtils enabled!");
        getLogger().info("Whitelist: " + (whitelistManager.isEnabled() ? "ON" : "OFF"));
        getLogger().info("AutoRestart: " + (autoRestartManager.isRunning() ? "ON" : "OFF"));
    }
    
    @Override
    public void onDisable() {
        if (dimensionLockManager != null) {
            dimensionLockManager.shutdown();
        }
        if (autoRestartManager != null) {
            autoRestartManager.stop();
        }
        if (whitelistManager != null) {
            whitelistManager.saveWhitelist();
        }
        getLogger().info("LoUtils disabled!");
    }
    
    public WhitelistManager getWhitelistManager() {
        return whitelistManager;
    }
    
    public AutoRestartManager getAutoRestartManager() {
        return autoRestartManager;
    }
    
    public DimensionLockManager getDimensionLockManager() {
        return dimensionLockManager;
    }
}

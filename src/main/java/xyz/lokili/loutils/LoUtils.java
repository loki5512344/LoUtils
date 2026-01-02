package xyz.lokili.loutils;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lokili.loutils.commands.*;
import xyz.lokili.loutils.listeners.*;
import xyz.lokili.loutils.managers.*;
import xyz.lokili.loutils.placeholders.LoUtilsExpansion;

public class LoUtils extends JavaPlugin {
    
    private ConfigManager configManager;
    private WhitelistManager whitelistManager;
    private AutoRestartManager autoRestartManager;
    private DimensionLockManager dimensionLockManager;
    private VanishManager vanishManager;
    private TPSBarManager tpsBarManager;
    
    @Override
    public void onEnable() {
        // Config manager first
        configManager = new ConfigManager(this);
        
        // Initialize managers
        whitelistManager = new WhitelistManager(this);
        autoRestartManager = new AutoRestartManager(this);
        dimensionLockManager = new DimensionLockManager(this);
        vanishManager = new VanishManager(this);
        tpsBarManager = new TPSBarManager(this);
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Start autorestart if enabled
        if (configManager.isModuleEnabled("autorestart")) {
            autoRestartManager.start();
        }
        
        // Register PlaceholderAPI expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LoUtilsExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        }
        
        getLogger().info("LoUtils enabled!");
        getLogger().info("Modules: Whitelist, AutoRestart, DimensionLock, Vanish, Enchant, DeathMessages, TPSBar, InvSee, SpawnMob");
    }
    
    @Override
    public void onDisable() {
        if (tpsBarManager != null) tpsBarManager.shutdown();
        if (dimensionLockManager != null) dimensionLockManager.shutdown();
        if (autoRestartManager != null) autoRestartManager.stop();
        if (vanishManager != null) vanishManager.saveData();
        if (whitelistManager != null) whitelistManager.saveWhitelist();
        
        getLogger().info("LoUtils disabled!");
    }
    
    private void registerCommands() {
        // Whitelist
        WhitelistCommand whitelistCommand = new WhitelistCommand(this);
        registerCommand("lw", whitelistCommand, whitelistCommand);
        
        // AutoRestart
        AutoRestartCommand autoRestartCommand = new AutoRestartCommand(this);
        registerCommand("lar", autoRestartCommand, autoRestartCommand);
        
        // Dimension Lock
        DimensionLockCommand dimensionLockCommand = new DimensionLockCommand(this);
        registerCommand("ll", dimensionLockCommand, dimensionLockCommand);
        
        // Vanish
        VanishCommand vanishCommand = new VanishCommand(this);
        registerCommand("lv", vanishCommand, vanishCommand);
        
        // SpawnMob
        SpawnMobCommand spawnMobCommand = new SpawnMobCommand(this);
        registerCommand("lspawnmob", spawnMobCommand, spawnMobCommand);
        
        // InvSee
        InvSeeCommand invSeeCommand = new InvSeeCommand(this);
        registerCommand("linvsee", invSeeCommand, invSeeCommand);
        
        // Enchant
        EnchantCommand enchantCommand = new EnchantCommand(this);
        registerCommand("lenchant", enchantCommand, enchantCommand);

        // Fly
        FlyCommand flyCommand = new FlyCommand(this);
        registerCommand("lfly", flyCommand, flyCommand);

        // FlySpeed
        FlySpeedCommand flySpeedCommand = new FlySpeedCommand(this);
        registerCommand("lflyspeed", flySpeedCommand, flySpeedCommand);
        
        // Main command
        LoUtilsCommand loUtilsCommand = new LoUtilsCommand(this);
        registerCommand("loutils", loUtilsCommand, loUtilsCommand);
        
        // TPSBar
        TPSBarCommand tpsBarCommand = new TPSBarCommand(this);
        registerCommand("ltpsbar", tpsBarCommand, tpsBarCommand);
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor,
                                 org.bukkit.command.TabCompleter tabCompleter) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command '" + name + "' is not defined in plugin.yml");
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(tabCompleter);
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathMessageListener(this), this);
    }
    
    public void reload() {
        configManager.reloadAll();
        whitelistManager.reload();
        autoRestartManager.reload();
    }
    
    // Getters
    public ConfigManager getConfigManager() { return configManager; }
    public WhitelistManager getWhitelistManager() { return whitelistManager; }
    public AutoRestartManager getAutoRestartManager() { return autoRestartManager; }
    public DimensionLockManager getDimensionLockManager() { return dimensionLockManager; }
    public VanishManager getVanishManager() { return vanishManager; }
    public TPSBarManager getTPSBarManager() { return tpsBarManager; }
}

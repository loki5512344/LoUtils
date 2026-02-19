package xyz.lokili.loutils;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.lokili.loutils.api.*;
import xyz.lokili.loutils.commands.*;
import xyz.lokili.loutils.listeners.*;
import xyz.lokili.loutils.managers.*;
import xyz.lokili.loutils.placeholders.LoUtilsExpansion;
import xyz.lokili.loutils.utils.MessageUtil;

public class LoUtils extends JavaPlugin {
    
    private IConfigManager configManager;
    private IWhitelistManager whitelistManager;
    private IAutoRestartManager autoRestartManager;
    private ITPSBarManager tpsBarManager;
    private IWorldLockManager worldLockManager;
    private ICustomWorldHeightManager customWorldHeightManager;
    private MessageUtil messageUtil;
    private InvSeeListener invSeeListener;
    
    @Override
    public void onEnable() {
        // Config manager first
        configManager = new ConfigManager(this);
        
        // Initialize utilities
        messageUtil = new MessageUtil(this);
        
        // Initialize managers
        whitelistManager = new WhitelistManager(this);
        autoRestartManager = new AutoRestartManager(this);
        tpsBarManager = new TPSBarManager(this);
        worldLockManager = new WorldLockManager(this);
        customWorldHeightManager = new CustomWorldHeightManager(this);
        
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
        
        getLogger().info("LoUtils v2.0.0 enabled!");
        getLogger().info("Modules: Whitelist, AutoRestart, Enchant, DeathMessages, TPSBar, InvSee, SpawnMob, Fly, WorldLock, CustomWorldHeight");
    }
    
    @Override
    public void onDisable() {
        if (invSeeListener != null) invSeeListener.shutdown();
        if (tpsBarManager != null) tpsBarManager.shutdown();
        if (autoRestartManager != null) autoRestartManager.stop();
        if (whitelistManager != null) whitelistManager.saveWhitelist();
        
        getLogger().info("LoUtils v2.0.0 disabled!");
    }
    
    private void registerCommands() {
        // Whitelist
        WhitelistCommand whitelistCommand = new WhitelistCommand(this);
        registerCommand("lw", whitelistCommand, whitelistCommand);
        
        // AutoRestart
        AutoRestartCommand autoRestartCommand = new AutoRestartCommand(this);
        registerCommand("lar", autoRestartCommand, autoRestartCommand);
        
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
        
        // WorldLock
        WorldLockCommand worldLockCommand = new WorldLockCommand(this);
        registerCommand("worldlock", worldLockCommand, worldLockCommand);
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
        invSeeListener = new InvSeeListener(this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathMessageListener(this), this);
        getServer().getPluginManager().registerEvents(invSeeListener, this);
        getServer().getPluginManager().registerEvents(new WorldLockListener(this), this);
        getServer().getPluginManager().registerEvents(new CustomWorldHeightListener(this), this);
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
    public InvSeeListener getInvSeeListener() { return invSeeListener; }
}

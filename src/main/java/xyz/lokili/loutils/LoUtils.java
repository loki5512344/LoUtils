package xyz.lokili.loutils;

import org.bukkit.Bukkit;
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
    private StatsManager statsManager;
    private PartyManager partyManager;
    
    @Override
    public void onEnable() {
        // Config manager first
        configManager = new ConfigManager(this);
        
        // Initialize managers
        whitelistManager = new WhitelistManager(this);
        autoRestartManager = new AutoRestartManager(this);
        dimensionLockManager = new DimensionLockManager(this);
        vanishManager = new VanishManager(this);
        statsManager = new StatsManager(this);
        partyManager = new PartyManager(this);
        
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
        getLogger().info("Modules: Whitelist, AutoRestart, DimensionLock, Vanish, Stats, Party, DeathMessages");
    }
    
    @Override
    public void onDisable() {
        if (partyManager != null) partyManager.shutdown();
        if (dimensionLockManager != null) dimensionLockManager.shutdown();
        if (autoRestartManager != null) autoRestartManager.stop();
        if (statsManager != null) statsManager.shutdown();
        if (vanishManager != null) vanishManager.saveData();
        if (whitelistManager != null) whitelistManager.saveWhitelist();
        
        getLogger().info("LoUtils disabled!");
    }
    
    private void registerCommands() {
        // Whitelist
        WhitelistCommand whitelistCommand = new WhitelistCommand(this);
        getCommand("lw").setExecutor(whitelistCommand);
        getCommand("lw").setTabCompleter(whitelistCommand);
        
        // AutoRestart
        AutoRestartCommand autoRestartCommand = new AutoRestartCommand(this);
        getCommand("lar").setExecutor(autoRestartCommand);
        getCommand("lar").setTabCompleter(autoRestartCommand);
        
        // Dimension Lock
        DimensionLockCommand dimensionLockCommand = new DimensionLockCommand(this);
        getCommand("ll").setExecutor(dimensionLockCommand);
        getCommand("ll").setTabCompleter(dimensionLockCommand);
        
        // Vanish
        VanishCommand vanishCommand = new VanishCommand(this);
        getCommand("lv").setExecutor(vanishCommand);
        getCommand("lv").setTabCompleter(vanishCommand);
        
        // Stats
        StatsCommand statsCommand = new StatsCommand(this);
        getCommand("lstats").setExecutor(statsCommand);
        getCommand("lstats").setTabCompleter(statsCommand);
        
        // SpawnMob
        SpawnMobCommand spawnMobCommand = new SpawnMobCommand(this);
        getCommand("lspawnmob").setExecutor(spawnMobCommand);
        getCommand("lspawnmob").setTabCompleter(spawnMobCommand);
        
        // InvSee
        InvSeeCommand invSeeCommand = new InvSeeCommand(this);
        getCommand("linvsee").setExecutor(invSeeCommand);
        getCommand("linvsee").setTabCompleter(invSeeCommand);
        
        // Party
        PartyCommand partyCommand = new PartyCommand(this);
        getCommand("lparty").setExecutor(partyCommand);
        getCommand("lparty").setTabCompleter(partyCommand);
        
        // Main command
        LoUtilsCommand loUtilsCommand = new LoUtilsCommand(this);
        getCommand("loutils").setExecutor(loUtilsCommand);
        getCommand("loutils").setTabCompleter(loUtilsCommand);
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        getServer().getPluginManager().registerEvents(new VanishListener(this), this);
        getServer().getPluginManager().registerEvents(new StatsListener(this), this);
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
    public StatsManager getStatsManager() { return statsManager; }
    public PartyManager getPartyManager() { return partyManager; }
}

package xyz.lokili.loutils.registry;

import org.bukkit.command.PluginCommand;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.*;

/**
 * Регистрация команд плагина
 */
public class CommandRegistry {
    
    private final LoUtils plugin;
    
    public CommandRegistry(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    public void registerAll() {
        // Whitelist
        WhitelistCommand whitelistCommand = new WhitelistCommand(plugin);
        register("lw", whitelistCommand, whitelistCommand);
        
        // AutoRestart
        AutoRestartCommand autoRestartCommand = new AutoRestartCommand(plugin);
        register("lar", autoRestartCommand, autoRestartCommand);
        
        // SpawnMob
        SpawnMobCommand spawnMobCommand = new SpawnMobCommand(plugin);
        register("lspawnmob", spawnMobCommand, spawnMobCommand);
        
        // InvSee
        InvSeeCommand invSeeCommand = new InvSeeCommand(plugin);
        register("linvsee", invSeeCommand, invSeeCommand);
        
        // Enchant
        EnchantCommand enchantCommand = new EnchantCommand(plugin);
        register("lenchant", enchantCommand, enchantCommand);

        // Fly
        FlyCommand flyCommand = new FlyCommand(plugin);
        register("lfly", flyCommand, flyCommand);

        // FlySpeed
        FlySpeedCommand flySpeedCommand = new FlySpeedCommand(plugin);
        register("lflyspeed", flySpeedCommand, flySpeedCommand);
        
        // Main command
        LoUtilsCommand loUtilsCommand = new LoUtilsCommand(plugin);
        register("loutils", loUtilsCommand, loUtilsCommand);
        
        // TPSBar
        TPSBarCommand tpsBarCommand = new TPSBarCommand(plugin);
        register("ltpsbar", tpsBarCommand, tpsBarCommand);
        
        // WorldLock
        WorldLockCommand worldLockCommand = new WorldLockCommand(plugin);
        register("worldlock", worldLockCommand, worldLockCommand);
    }
    
    private void register(String name, org.bukkit.command.CommandExecutor executor,
                         org.bukkit.command.TabCompleter tabCompleter) {
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            plugin.loLogger().error("Command '" + name + "' is not defined in plugin.yml");
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(tabCompleter);
    }
}

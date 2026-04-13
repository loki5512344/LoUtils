package xyz.lokili.loutils.registry;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.admin.AutoRestartCommand;
import xyz.lokili.loutils.commands.admin.ColorFixCommand;
import xyz.lokili.loutils.commands.admin.ColorTestCommand;
import xyz.lokili.loutils.commands.admin.LoUtilsCommand;
import xyz.lokili.loutils.commands.admin.WhitelistCommand;
import xyz.lokili.loutils.commands.gameplay.EnchantCommand;
import xyz.lokili.loutils.commands.gameplay.MapCommand;
import xyz.lokili.loutils.commands.player.*;
import xyz.lokili.loutils.commands.world.FastLeafDecayCommand;
import xyz.lokili.loutils.commands.world.WorldLockCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Регистрация команд плагина
 * Использует Map для автоматизации (DRY + Open/Closed принцип)
 */
public class CommandRegistry {

    private final LoUtils plugin;
    private final Map<String, Function<LoUtils, CommandExecutor>> commandFactories = new HashMap<>();

    public CommandRegistry(LoUtils plugin) {
        this.plugin = plugin;
        initializeFactories();
    }

    /**
     * Инициализация фабрик команд
     * Для добавления новой команды достаточно добавить одну строку здесь
     */
    private void initializeFactories() {
        // Admin commands
        commandFactories.put("lw", WhitelistCommand::new);
        commandFactories.put("lar", AutoRestartCommand::new);
        commandFactories.put("loutils", LoUtilsCommand::new);
        commandFactories.put("lcolortest", ColorTestCommand::new);
        commandFactories.put("lcolorfix", ColorFixCommand::new);

        // Player commands
        commandFactories.put("lspawnmob", SpawnMobCommand::new);
        commandFactories.put("linvsee", InvSeeCommand::new);
        commandFactories.put("lfly", FlyCommand::new);
        commandFactories.put("lflyspeed", FlySpeedCommand::new);
        commandFactories.put("ltpsbar", TPSBarCommand::new);
        commandFactories.put("sit", SitCommand::new);
        commandFactories.put("pose", PoseCommand::new);

        // Gameplay commands
        commandFactories.put("lenchant", EnchantCommand::new);
        commandFactories.put("map", MapCommand::new);

        // World commands
        commandFactories.put("lfastleaf", FastLeafDecayCommand::new);
        commandFactories.put("worldlock", WorldLockCommand::new);
    }

    /**
     * Регистрирует все команды автоматически
     */
    public void registerAll() {
        for (Map.Entry<String, Function<LoUtils, CommandExecutor>> entry : commandFactories.entrySet()) {
            String commandName = entry.getKey();
            CommandExecutor executor = entry.getValue().apply(plugin);
            register(commandName, executor, (TabCompleter) executor);
        }
    }

    /**
     * Регистрирует одну команду
     */
    private void register(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            plugin.loLogger().error("Command '" + name + "' is not defined in plugin.yml");
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(tabCompleter);
    }
}

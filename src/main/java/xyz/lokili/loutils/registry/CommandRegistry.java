package xyz.lokili.loutils.registry;

import org.bukkit.command.PluginCommand;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.admin.AutoRestartCommand;
import xyz.lokili.loutils.commands.admin.ColorFixCommand;
import xyz.lokili.loutils.commands.admin.ColorTestCommand;
import xyz.lokili.loutils.commands.admin.LoUtilsCommand;
import xyz.lokili.loutils.commands.admin.WhitelistCommand;
import xyz.lokili.loutils.commands.gameplay.EnchantCommand;
import xyz.lokili.loutils.commands.gameplay.MapCommand;
import xyz.lokili.loutils.commands.player.FlyCommand;
import xyz.lokili.loutils.commands.player.FlySpeedCommand;
import xyz.lokili.loutils.commands.player.InvSeeCommand;
import xyz.lokili.loutils.commands.player.PoseCommand;
import xyz.lokili.loutils.commands.player.SitCommand;
import xyz.lokili.loutils.commands.player.SpawnMobCommand;
import xyz.lokili.loutils.commands.player.TPSBarCommand;
import xyz.lokili.loutils.commands.world.FastLeafDecayCommand;
import xyz.lokili.loutils.commands.world.WorldLockCommand;

/**
 * Регистрация команд плагина
 */
public class CommandRegistry {

    private final LoUtils plugin;

    public CommandRegistry(LoUtils plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        WhitelistCommand whitelistCommand = new WhitelistCommand(plugin);
        register("lw", whitelistCommand, whitelistCommand);

        AutoRestartCommand autoRestartCommand = new AutoRestartCommand(plugin);
        register("lar", autoRestartCommand, autoRestartCommand);

        SpawnMobCommand spawnMobCommand = new SpawnMobCommand(plugin);
        register("lspawnmob", spawnMobCommand, spawnMobCommand);

        InvSeeCommand invSeeCommand = new InvSeeCommand(plugin);
        register("linvsee", invSeeCommand, invSeeCommand);

        EnchantCommand enchantCommand = new EnchantCommand(plugin);
        register("lenchant", enchantCommand, enchantCommand);

        FlyCommand flyCommand = new FlyCommand(plugin);
        register("lfly", flyCommand, flyCommand);

        FlySpeedCommand flySpeedCommand = new FlySpeedCommand(plugin);
        register("lflyspeed", flySpeedCommand, flySpeedCommand);

        LoUtilsCommand loUtilsCommand = new LoUtilsCommand(plugin);
        register("loutils", loUtilsCommand, loUtilsCommand);

        TPSBarCommand tpsBarCommand = new TPSBarCommand(plugin);
        register("ltpsbar", tpsBarCommand, tpsBarCommand);

        ColorTestCommand colorTestCommand = new ColorTestCommand(plugin);
        register("lcolortest", colorTestCommand, colorTestCommand);

        ColorFixCommand colorFixCommand = new ColorFixCommand(plugin);
        register("lcolorfix", colorFixCommand, colorFixCommand);

        FastLeafDecayCommand fastLeafDecayCommand = new FastLeafDecayCommand(plugin);
        register("lfastleaf", fastLeafDecayCommand, fastLeafDecayCommand);

        WorldLockCommand worldLockCommand = new WorldLockCommand(plugin);
        register("worldlock", worldLockCommand, worldLockCommand);

        SitCommand sitCommand = new SitCommand(plugin);
        register("sit", sitCommand, sitCommand);

        PoseCommand poseCommand = new PoseCommand(plugin);
        register("pose", poseCommand, poseCommand);

        MapCommand mapCommand = new MapCommand(plugin);
        register("map", mapCommand, mapCommand);
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

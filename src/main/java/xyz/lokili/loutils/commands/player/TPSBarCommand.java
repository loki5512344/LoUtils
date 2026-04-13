package xyz.lokili.loutils.commands.player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.util.Arrays;
import java.util.List;

public class TPSBarCommand extends CommandBase {
    
    public TPSBarCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, ConfigConstants.Permissions.TPSBAR)) {
            return true;
        }
        
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        
        if (args.length == 0) {
            // Toggle
            plugin.getContainer().getTPSBarManager().toggleTPSBar(player);

            if (plugin.getContainer().getTPSBarManager().hasTPSBar(player)) {
                sendConfigMessage(player, ConfigConstants.TPSBAR_CONFIG, "messages.enabled");
            } else {
                sendConfigMessage(player, ConfigConstants.TPSBAR_CONFIG, "messages.disabled");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "on", "enable" -> {
                if (!plugin.getContainer().getTPSBarManager().hasTPSBar(player)) {
                    plugin.getContainer().getTPSBarManager().enableTPSBar(player);
                    sendConfigMessage(player, ConfigConstants.TPSBAR_CONFIG, "messages.enabled");
                } else {
                    sendConfigMessage(player, ConfigConstants.TPSBAR_CONFIG, "messages.already-enabled");
                }
            }
            case "off", "disable" -> {
                if (plugin.getContainer().getTPSBarManager().hasTPSBar(player)) {
                    plugin.getContainer().getTPSBarManager().disableTPSBar(player);
                    sendConfigMessage(player, ConfigConstants.TPSBAR_CONFIG, "messages.disabled");
                } else {
                    sendConfigMessage(player, ConfigConstants.TPSBAR_CONFIG, "messages.already-disabled");
                }
            }
            default -> sendConfigMessage(player, ConfigConstants.TPSBAR_CONFIG, "messages.usage");
        }
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ConfigConstants.Permissions.TPSBAR)) {
            return List.of();
        }
        
        if (args.length == 1) {
            return filterTabComplete(Arrays.asList("on", "off"), args[0]);
        }
        
        return List.of();
    }
}

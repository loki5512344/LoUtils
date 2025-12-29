package xyz.lokili.loutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TPSBarCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    
    public TPSBarCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.tpsbar")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        
        if (args.length == 0) {
            // Toggle
            plugin.getTPSBarManager().toggleTPSBar(player);
            
            if (plugin.getTPSBarManager().hasTPSBar(player)) {
                sendConfigMessage(player, "enabled");
            } else {
                sendConfigMessage(player, "disabled");
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "on", "enable" -> {
                if (!plugin.getTPSBarManager().hasTPSBar(player)) {
                    plugin.getTPSBarManager().enableTPSBar(player);
                    sendConfigMessage(player, "enabled");
                } else {
                    sendConfigMessage(player, "already-enabled");
                }
            }
            case "off", "disable" -> {
                if (plugin.getTPSBarManager().hasTPSBar(player)) {
                    plugin.getTPSBarManager().disableTPSBar(player);
                    sendConfigMessage(player, "disabled");
                } else {
                    sendConfigMessage(player, "already-disabled");
                }
            }
            default -> sendConfigMessage(player, "usage");
        }
        
        return true;
    }
    
    private void sendMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    private void sendConfigMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getConfig("conf/tpsbar.yml")
                .getString("messages." + key, "Message not found: " + key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("loutils.tpsbar")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("on", "off").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        return new ArrayList<>();
    }
}

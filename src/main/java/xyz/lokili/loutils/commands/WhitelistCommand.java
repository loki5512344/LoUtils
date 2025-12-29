package xyz.lokili.loutils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class WhitelistCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    private final List<String> subCommands = Arrays.asList("add", "remove", "list", "enable", "disable", "reload");
    
    public WhitelistCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.whitelist")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendMessage(sender, "whitelist.usage");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "enable" -> handleEnable(sender);
            case "disable" -> handleDisable(sender);
            case "reload" -> handleReload(sender);
            default -> sendMessage(sender, "whitelist.usage");
        }
        
        return true;
    }
    
    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "player-required");
            return;
        }
        
        String playerName = args[1];
        
        if (plugin.getWhitelistManager().addPlayer(playerName)) {
            sendMessage(sender, "whitelist.player-added", "{player}", playerName);
        } else {
            sendMessage(sender, "whitelist.player-already-in-list", "{player}", playerName);
        }
    }
    
    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "player-required");
            return;
        }
        
        String playerName = args[1];
        
        if (plugin.getWhitelistManager().removePlayer(playerName)) {
            sendMessage(sender, "whitelist.player-removed", "{player}", playerName);
        } else {
            sendMessage(sender, "whitelist.player-not-in-list", "{player}", playerName);
        }
    }
    
    private void handleList(CommandSender sender) {
        Set<String> players = plugin.getWhitelistManager().getWhitelistedPlayers();
        int count = players.size();
        
        String header = plugin.getConfigManager().getMessage("whitelist.list-header")
                .replace("{count}", String.valueOf(count));
        sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + header));
        
        if (players.isEmpty()) {
            sendMessage(sender, "whitelist.list-empty");
        } else {
            for (String player : players) {
                String line = plugin.getConfigManager().getMessage("whitelist.list-player")
                        .replace("{player}", player);
                sender.sendMessage(ColorUtil.colorize(line));
            }
        }
    }
    
    private void handleEnable(CommandSender sender) {
        if (plugin.getWhitelistManager().isEnabled()) {
            sendMessage(sender, "whitelist.already-enabled");
            return;
        }
        
        plugin.getWhitelistManager().setEnabled(true);
        sendMessage(sender, "whitelist.enabled");
    }
    
    private void handleDisable(CommandSender sender) {
        if (!plugin.getWhitelistManager().isEnabled()) {
            sendMessage(sender, "whitelist.already-disabled");
            return;
        }
        
        plugin.getWhitelistManager().setEnabled(false);
        sendMessage(sender, "whitelist.disabled");
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getWhitelistManager().reload();
        sendMessage(sender, "config-reloaded");
    }
    
    private void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, null, null);
    }
    
    private void sendMessage(CommandSender sender, String key, String placeholder, String value) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(key);
        
        if (placeholder != null && value != null) {
            message = message.replace(placeholder, value);
        }
        
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.whitelist")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return plugin.getWhitelistManager().getWhitelistedPlayers().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        
        return new ArrayList<>();
    }
}

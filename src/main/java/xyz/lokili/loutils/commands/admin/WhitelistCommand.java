package xyz.lokili.loutils.commands.admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class WhitelistCommand extends CommandBase {
    
    private final List<String> subCommands = Arrays.asList("add", "remove", "list", "enable", "disable", "reload");
    
    public WhitelistCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, ConfigConstants.Permissions.WHITELIST)) {
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
        
        sendMessage(sender, "whitelist.list-header", "{count}", String.valueOf(count));
        
        if (players.isEmpty()) {
            sendMessage(sender, "whitelist.list-empty");
        } else {
            for (String player : players) {
                sendMessage(sender, "whitelist.list-player", "{player}", player);
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
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        
        if (!sender.hasPermission(ConfigConstants.Permissions.WHITELIST)) {
            return List.of();
        }
        
        if (args.length == 1) {
            return filterTabComplete(subCommands, args[0]);
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return filterTabComplete(
                    plugin.getWhitelistManager().getWhitelistedPlayers().stream().toList(),
                    args[1]
            );
        }
        
        return List.of();
    }
}

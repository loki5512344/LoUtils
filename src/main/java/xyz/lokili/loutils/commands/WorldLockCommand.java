package xyz.lokili.loutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
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

public class WorldLockCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    private final List<String> subCommands = Arrays.asList("add", "remove", "list", "reload");
    
    public WorldLockCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.worldlock")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendConfigMessage(sender, "usage");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> sendConfigMessage(sender, "usage");
        }
        
        return true;
    }
    
    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendConfigMessage(sender, "usage");
            return;
        }
        
        for (int i = 1; i < args.length; i++) {
            String worldName = args[i];
            
            if (plugin.getWorldLockManager().addWorld(worldName)) {
                sendConfigMessage(sender, "world-added", "{world}", worldName);
            } else {
                sendConfigMessage(sender, "world-already-locked", "{world}", worldName);
            }
        }
    }
    
    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendConfigMessage(sender, "usage");
            return;
        }
        
        for (int i = 1; i < args.length; i++) {
            String worldName = args[i];
            
            if (plugin.getWorldLockManager().removeWorld(worldName)) {
                sendConfigMessage(sender, "world-removed", "{world}", worldName);
            } else {
                sendConfigMessage(sender, "world-not-locked", "{world}", worldName);
            }
        }
    }
    
    private void handleList(CommandSender sender) {
        Set<String> lockedWorlds = plugin.getWorldLockManager().getLockedWorlds();
        
        if (lockedWorlds.isEmpty()) {
            sendConfigMessage(sender, "no-locked-worlds");
            return;
        }
        
        sendConfigMessage(sender, "locked-worlds-list");
        String worlds = String.join("&8, &f", lockedWorlds);
        sender.sendMessage(ColorUtil.colorize("&f" + worlds));
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getWorldLockManager().reload();
        sendMessage(sender, "config-reloaded");
    }
    
    private void sendMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    private void sendConfigMessage(CommandSender sender, String key) {
        sendConfigMessage(sender, key, null, null);
    }
    
    private void sendConfigMessage(CommandSender sender, String key, String placeholder, String value) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getConfig("conf/worldlock.yml")
                .getString("messages." + key, "Message not found: " + key);
        
        if (placeholder != null && value != null) {
            message = message.replace(placeholder, value);
        }
        
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("loutils.worldlock")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("add")) {
                // Показываем все миры
                return Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                        .toList();
            } else if (args[0].equalsIgnoreCase("remove")) {
                // Показываем только заблокированные миры
                return plugin.getWorldLockManager().getLockedWorlds().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                        .toList();
            }
        }
        
        return new ArrayList<>();
    }
}

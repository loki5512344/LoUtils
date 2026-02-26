package xyz.lokili.loutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
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

public class WorldLockCommand extends CommandBase {
    
    private final List<String> subCommands = Arrays.asList("add", "remove", "list", "reload");
    
    public WorldLockCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, ConfigConstants.Permissions.WORLDLOCK)) {
            return true;
        }
        
        if (args.length == 0) {
            sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, "messages.usage");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            default -> sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, "messages.usage");
        }
        
        return true;
    }
    
    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, "messages.usage");
            return;
        }
        
        for (int i = 1; i < args.length; i++) {
            String worldName = args[i];
            
            if (plugin.getWorldLockManager().addWorld(worldName)) {
                sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, 
                        "messages.world-added", "{world}", worldName);
            } else {
                sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, 
                        "messages.world-already-locked", "{world}", worldName);
            }
        }
    }
    
    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, "messages.usage");
            return;
        }
        
        for (int i = 1; i < args.length; i++) {
            String worldName = args[i];
            
            if (plugin.getWorldLockManager().removeWorld(worldName)) {
                sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, 
                        "messages.world-removed", "{world}", worldName);
            } else {
                sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, 
                        "messages.world-not-locked", "{world}", worldName);
            }
        }
    }
    
    private void handleList(CommandSender sender) {
        Set<String> lockedWorlds = plugin.getWorldLockManager().getLockedWorlds();
        
        if (lockedWorlds.isEmpty()) {
            sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, "messages.no-locked-worlds");
            return;
        }
        
        sendConfigMessage(sender, ConfigConstants.WORLDLOCK_CONFIG, "messages.locked-worlds-list");
        String worlds = String.join("&8, &f", lockedWorlds);
        sendRawMessage(sender, "&f" + worlds);
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getWorldLockManager().reload();
        sendMessage(sender, "config-reloaded");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ConfigConstants.Permissions.WORLDLOCK)) {
            return List.of();
        }
        
        if (args.length == 1) {
            return filterTabComplete(subCommands, args[0]);
        }
        
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("add")) {
                List<String> worldNames = Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .toList();
                return filterTabComplete(worldNames, args[args.length - 1]);
            } else if (args[0].equalsIgnoreCase("remove")) {
                return filterTabComplete(
                        plugin.getWorldLockManager().getLockedWorlds().stream().toList(),
                        args[args.length - 1]
                );
            }
        }
        
        return List.of();
    }
}

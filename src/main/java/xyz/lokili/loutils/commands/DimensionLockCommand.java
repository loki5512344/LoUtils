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

public class DimensionLockCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    private final List<String> subCommands = Arrays.asList("lock", "unlock", "status", "reload");
    private final List<String> dimensions = Arrays.asList("nether", "end");
    
    public DimensionLockCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.lock.command")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendMessage(sender, "usage-dimensionlock");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "lock" -> handleLock(sender, args);
            case "unlock" -> handleUnlock(sender, args);
            case "status" -> handleStatus(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendMessage(sender, "usage-dimensionlock");
        }
        
        return true;
    }
    
    private void handleLock(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "dimension-required");
            return;
        }
        
        String dimension = args[1].toLowerCase();
        int time = plugin.getConfig().getInt("dimensionlock.default_lock_time", 10);
        
        if (args.length >= 3) {
            try {
                time = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sendMessage(sender, "invalid-time");
                return;
            }
        }
        
        if (plugin.getDimensionLockManager().lockDimension(dimension, time)) {
            String message = getMessage("dimension-locked")
                    .replace("{dimension}", plugin.getDimensionLockManager().getDimensionDisplayName(dimension))
                    .replace("{time}", String.valueOf(time));
            sendRawMessage(sender, message);
        } else {
            sendMessage(sender, "invalid-dimension");
        }
    }
    
    private void handleUnlock(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "dimension-required");
            return;
        }
        
        String dimension = args[1].toLowerCase();
        
        if (plugin.getDimensionLockManager().unlockDimension(dimension)) {
            String message = getMessage("dimension-unlocked")
                    .replace("{dimension}", plugin.getDimensionLockManager().getDimensionDisplayName(dimension));
            sendRawMessage(sender, message);
        } else {
            sendMessage(sender, "dimension-not-locked");
        }
    }
    
    private void handleStatus(CommandSender sender, String[] args) {
        if (args.length < 2) {
            // Показать статус всех измерений
            sendStatusAll(sender);
            return;
        }
        
        String dimension = args[1].toLowerCase();
        
        if (plugin.getDimensionLockManager().isLocked(dimension)) {
            String message = getMessage("dimension-status-locked")
                    .replace("{dimension}", plugin.getDimensionLockManager().getDimensionDisplayName(dimension))
                    .replace("{time}", plugin.getDimensionLockManager().getTimeRemainingFormatted(dimension));
            sendRawMessage(sender, message);
        } else {
            String message = getMessage("dimension-status-open")
                    .replace("{dimension}", plugin.getDimensionLockManager().getDimensionDisplayName(dimension));
            sendRawMessage(sender, message);
        }
    }
    
    private void sendStatusAll(CommandSender sender) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        sender.sendMessage(ColorUtil.colorize(prefix + "&#3BA8FF=== Статус измерений ==="));
        
        for (String dim : dimensions) {
            if (plugin.getDimensionLockManager().isLocked(dim)) {
                String message = "&7" + plugin.getDimensionLockManager().getDimensionDisplayName(dim) + 
                        ": &cзакрыт &7(" + plugin.getDimensionLockManager().getTimeRemainingFormatted(dim) + ")";
                sender.sendMessage(ColorUtil.colorize(message));
            } else {
                String message = "&7" + plugin.getDimensionLockManager().getDimensionDisplayName(dim) + ": &aоткрыт";
                sender.sendMessage(ColorUtil.colorize(message));
            }
        }
    }
    
    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        sendMessage(sender, "config-reloaded");
    }
    
    private void sendMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String message = getMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    private void sendRawMessage(CommandSender sender, String message) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    private String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.lock.command")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("lock") || 
                args[0].equalsIgnoreCase("unlock") || args[0].equalsIgnoreCase("status"))) {
            return dimensions.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        
        return new ArrayList<>();
    }
}

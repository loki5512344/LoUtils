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

public class AutoRestartCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    private final List<String> subCommands = Arrays.asList("start", "stop", "status", "reload");
    
    public AutoRestartCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.autorestart.command")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendMessage(sender, "usage-autorestart");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start" -> handleStart(sender);
            case "stop" -> handleStop(sender);
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            default -> sendMessage(sender, "usage-autorestart");
        }
        
        return true;
    }
    
    private void handleStart(CommandSender sender) {
        if (plugin.getAutoRestartManager().isRunning()) {
            sendMessage(sender, "timer-already-running");
            return;
        }
        
        plugin.getConfig().set("autorestart.enabled", true);
        plugin.saveConfig();
        plugin.getAutoRestartManager().start();
        sendMessage(sender, "timer-started");
    }
    
    private void handleStop(CommandSender sender) {
        if (!plugin.getAutoRestartManager().isRunning()) {
            sendMessage(sender, "timer-not-running");
            return;
        }
        
        plugin.getAutoRestartManager().stop();
        sendMessage(sender, "timer-stopped");
    }
    
    private void handleStatus(CommandSender sender) {
        if (!plugin.getAutoRestartManager().isRunning()) {
            sendMessage(sender, "timer-status-disabled");
            return;
        }
        
        long[] parts = plugin.getAutoRestartManager().getTimeRemainingParts();
        String message = getMessage("timer-status")
                .replace("%hours%", String.valueOf(parts[0]))
                .replace("%minutes%", String.valueOf(parts[1]))
                .replace("%seconds%", String.valueOf(parts[2]));
        
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getAutoRestartManager().reload();
        sendMessage(sender, "config-reloaded");
    }
    
    private void sendMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String message = getMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    private String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.autorestart.command")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        return new ArrayList<>();
    }
}

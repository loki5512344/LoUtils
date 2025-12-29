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
        
        if (!sender.hasPermission("loutils.autorestart")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            sendMessage(sender, "autorestart.usage");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start" -> handleStart(sender);
            case "stop" -> handleStop(sender);
            case "status" -> handleStatus(sender);
            case "reload" -> handleReload(sender);
            default -> sendMessage(sender, "autorestart.usage");
        }
        
        return true;
    }
    
    private void handleStart(CommandSender sender) {
        if (plugin.getAutoRestartManager().isRunning()) {
            sendMessage(sender, "autorestart.timer-already-running");
            return;
        }
        
        plugin.getConfigManager().getAutoRestartConfig().set("enabled", true);
        plugin.getConfigManager().saveConfig("conf/autorestart.yml");
        plugin.getAutoRestartManager().start();
        sendMessage(sender, "autorestart.timer-started");
    }
    
    private void handleStop(CommandSender sender) {
        if (!plugin.getAutoRestartManager().isRunning()) {
            sendMessage(sender, "autorestart.timer-not-running");
            return;
        }
        
        plugin.getAutoRestartManager().stop();
        sendMessage(sender, "autorestart.timer-stopped");
    }
    
    private void handleStatus(CommandSender sender) {
        if (!plugin.getAutoRestartManager().isRunning()) {
            sendMessage(sender, "autorestart.timer-disabled");
            return;
        }
        
        long[] parts = plugin.getAutoRestartManager().getTimeRemainingParts();
        String message = plugin.getConfigManager().getMessage("autorestart.timer-status")
                .replace("{hours}", String.valueOf(parts[0]))
                .replace("{minutes}", String.valueOf(parts[1]))
                .replace("{seconds}", String.valueOf(parts[2]));
        
        sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + message));
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadAll();
        plugin.getAutoRestartManager().reload();
        sendMessage(sender, "config-reloaded");
    }
    
    private void sendMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.autorestart")) {
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

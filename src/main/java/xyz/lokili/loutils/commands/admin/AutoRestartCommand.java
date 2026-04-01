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

public class AutoRestartCommand extends CommandBase {
    
    private final List<String> subCommands = Arrays.asList("start", "stop", "status", "reload");
    
    public AutoRestartCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, ConfigConstants.Permissions.AUTORESTART)) {
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
        plugin.getConfigManager().saveConfig(ConfigConstants.AUTORESTART_CONFIG);
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
        sendMessage(sender, "autorestart.timer-status",
                "{hours}", String.valueOf(parts[0]),
                "{minutes}", String.valueOf(parts[1]),
                "{seconds}", String.valueOf(parts[2]));
    }
    
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadAll();
        plugin.getAutoRestartManager().reload();
        sendMessage(sender, "config-reloaded");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        
        if (!sender.hasPermission(ConfigConstants.Permissions.AUTORESTART)) {
            return List.of();
        }
        
        if (args.length == 1) {
            return filterTabComplete(subCommands, args[0]);
        }
        
        return List.of();
    }
}

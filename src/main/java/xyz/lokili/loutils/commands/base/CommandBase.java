package xyz.lokili.loutils.commands.base;

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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for all commands
 * Provides common functionality (DRY principle)
 */
public abstract class CommandBase implements CommandExecutor, TabCompleter {
    
    protected final LoUtils plugin;
    
    public CommandBase(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    // === Permission Checks ===
    
    protected boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sendMessage(sender, "no-permission");
            return false;
        }
        return true;
    }
    
    // === Player Checks ===
    
    protected Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "player-only");
            return null;
        }
        return (Player) sender;
    }
    
    // === Message Sending ===
    
    protected void sendMessage(CommandSender sender, String messageKey) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(messageKey);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    protected void sendMessage(CommandSender sender, String messageKey, String... replacements) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(messageKey, replacements);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    protected void sendConfigMessage(CommandSender sender, String configPath, String messageKey) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getConfig(configPath)
                .getString(messageKey, "&cMessage not found");
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    protected void sendConfigMessage(CommandSender sender, String configPath, 
                                     String messageKey, String... replacements) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getConfig(configPath)
                .getString(messageKey, "&cMessage not found");
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    protected void sendRawMessage(CommandSender sender, String message) {
        sender.sendMessage(ColorUtil.colorize(message));
    }
    
    // === Tab Completion Helpers ===
    
    protected List<String> filterTabComplete(List<String> options, String arg) {
        if (arg.isEmpty()) {
            return options;
        }
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(arg.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    protected List<String> getOnlinePlayerNames() {
        return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
    
    // === Abstract Methods ===
    
    @Override
    public abstract boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                     @NotNull String label, @NotNull String[] args);
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}

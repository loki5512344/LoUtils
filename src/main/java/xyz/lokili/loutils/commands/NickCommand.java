package xyz.lokili.loutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.managers.NickManager;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NickCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    
    public NickCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.nick")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        
        if (args.length == 0) {
            sendConfigMessage(sender, "usage");
            return true;
        }
        
        // Reset nick
        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length > 1 && sender.hasPermission("loutils.nick.others")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sendMessage(sender, "player-not-found");
                    return true;
                }
                plugin.getNickManager().resetNick(target);
                sendConfigMessage(sender, "reset-other", "{player}", target.getName());
            } else {
                plugin.getNickManager().resetNick(player);
                sendConfigMessage(sender, "reset");
            }
            return true;
        }
        
        // Set nick
        String nick = String.join(" ", args);
        
        NickManager.NickResult result = plugin.getNickManager().setNick(player, nick);
        FileConfiguration config = plugin.getConfigManager().getConfig("conf/nick.yml");
        
        switch (result) {
            case SUCCESS -> {
                String msg = config.getString("messages.set", "Ник изменён на: {nick}")
                        .replace("{nick}", nick);
                sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + msg));
            }
            case TOO_LONG -> {
                String msg = config.getString("messages.too-long", "Ник слишком длинный")
                        .replace("{max}", String.valueOf(config.getInt("max_length", 24)));
                sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + msg));
            }
            case TOO_SHORT -> {
                String msg = config.getString("messages.too-short", "Ник слишком короткий")
                        .replace("{min}", String.valueOf(config.getInt("min_length", 3)));
                sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + msg));
            }
            case INVALID_CHARS -> sendConfigMessage(sender, "invalid-chars");
            case BLACKLISTED -> sendConfigMessage(sender, "blacklisted");
        }
        
        return true;
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
        FileConfiguration config = plugin.getConfigManager().getConfig("conf/nick.yml");
        String prefix = plugin.getConfigManager().getPrefix();
        String message = config.getString("messages." + key, "Message not found: " + key);
        
        if (placeholder != null && value != null) {
            message = message.replace(placeholder, value);
        }
        
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("loutils.nick")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("reset");
            if (sender instanceof Player player) {
                suggestions.add(player.getName());
            }
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        return new ArrayList<>();
    }
}

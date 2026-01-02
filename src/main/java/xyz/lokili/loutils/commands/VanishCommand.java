package xyz.lokili.loutils.commands;

import org.bukkit.Bukkit;
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

public class VanishCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    
    public VanishCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.vanish")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        Player target;
        
        if (args.length > 0) {
            // Проверка статуса
            if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("check")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can check vanish status");
                    return true;
                }
                Player player = (Player) sender;
                boolean vanished = plugin.getVanishManager().isVanished(player);
                sender.sendMessage("§7Vanish status: " + (vanished ? "§aEnabled" : "§cDisabled"));
                sender.sendMessage("§7Vanish.see permission: " + (player.hasPermission("loutils.vanish.see") ? "§aYes" : "§cNo"));
                return true;
            }
            
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, "player-not-found");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "vanish.usage");
                return true;
            }
            target = (Player) sender;
        }
        
        plugin.getVanishManager().toggleVanish(target);
        
        boolean vanished = plugin.getVanishManager().isVanished(target);
        
        if (target.equals(sender)) {
            String key = vanished ? "enabled" : "disabled";
            sendVanishMessage(sender, key);
        } else {
            String key = vanished ? "enabled-other" : "disabled-other";
            sendVanishMessage(sender, key, "{player}", target.getName());
        }
        
        return true;
    }
    
    private void sendMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    private void sendVanishMessage(CommandSender sender, String key) {
        sendVanishMessage(sender, key, null, null);
    }
    
    private void sendVanishMessage(CommandSender sender, String key, String placeholder, String value) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getVanishConfig().getString("messages." + key, 
                "Message not found: " + key);
        
        if (placeholder != null && value != null) {
            message = message.replace(placeholder, value);
        }
        
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("loutils.vanish")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        return new ArrayList<>();
    }
}

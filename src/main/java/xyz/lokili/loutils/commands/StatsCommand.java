package xyz.lokili.loutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.managers.StatsManager;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class StatsCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    
    public StatsCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.stats")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        OfflinePlayer target;
        
        if (args.length > 0) {
            if (!sender.hasPermission("loutils.stats.others")) {
                sendMessage(sender, "no-permission");
                return true;
            }
            target = Bukkit.getOfflinePlayer(args[0]);
        } else {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "stats.usage");
                return true;
            }
            target = (Player) sender;
        }
        
        StatsManager.PlayerStats stats = plugin.getStatsManager().getStats(target.getUniqueId());
        String prefix = plugin.getConfigManager().getPrefix();
        
        // Header
        sender.sendMessage(ColorUtil.colorize(prefix + 
                plugin.getConfigManager().getMessage("stats.header")
                        .replace("{player}", target.getName())));
        
        // Playtime
        sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getMessage("stats.playtime")
                        .replace("{time}", plugin.getStatsManager().formatPlaytime(stats.playtime))));
        
        // Kills
        sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getMessage("stats.kills")
                        .replace("{kills}", String.valueOf(stats.kills))));
        
        // Deaths
        sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getMessage("stats.deaths")
                        .replace("{deaths}", String.valueOf(stats.deaths))));
        
        // KDR
        sender.sendMessage(ColorUtil.colorize(
                plugin.getConfigManager().getMessage("stats.kdr")
                        .replace("{kdr}", String.valueOf(stats.getKDR()))));
        
        return true;
    }
    
    private void sendMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("loutils.stats.others")) {
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

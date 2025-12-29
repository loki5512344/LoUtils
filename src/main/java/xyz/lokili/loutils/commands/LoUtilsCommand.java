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
import java.util.List;

public class LoUtilsCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    
    public LoUtilsCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.admin")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ColorUtil.colorize("&#3BA8FF&lLoUtils &7v" + plugin.getDescription().getVersion()));
            sender.sendMessage(ColorUtil.colorize("&7Автор: &floki"));
            sender.sendMessage(ColorUtil.colorize("&7Использование: &f/loutils reload"));
            return true;
        }
        
        plugin.reload();
        sendMessage(sender, "config-reloaded");
        
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
        if (!sender.hasPermission("loutils.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return List.of("reload");
        }
        
        return new ArrayList<>();
    }
}

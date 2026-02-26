package xyz.lokili.loutils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.util.List;

public class LoUtilsCommand extends CommandBase {
    
    public LoUtilsCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, ConfigConstants.Permissions.ADMIN)) {
            return true;
        }
        
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sendRawMessage(sender, "&#3BA8FF&lLoUtils &7v" + plugin.getDescription().getVersion());
            sendRawMessage(sender, "&7Автор: &floki");
            sendRawMessage(sender, "&7Использование: &f/loutils reload");
            return true;
        }
        
        plugin.reload();
        sendMessage(sender, "config-reloaded");
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ConfigConstants.Permissions.ADMIN)) {
            return List.of();
        }
        
        if (args.length == 1) {
            return filterTabComplete(List.of("reload"), args[0]);
        }
        
        return List.of();
    }
}

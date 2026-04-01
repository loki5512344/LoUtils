package xyz.lokili.loutils.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.util.List;

public class FlyCommand extends CommandBase {

    public FlyCommand(LoUtils plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!checkPermission(sender, ConfigConstants.Permissions.FLY)) {
            return true;
        }

        Player target;
        if (args.length >= 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sendMessage(sender, "player-not-found");
                return true;
            }
        } else {
            target = requirePlayer(sender);
            if (target == null) {
                return true;
            }
        }

        boolean newState = !target.getAllowFlight();
        target.setAllowFlight(newState);
        if (!newState) {
            target.setFlying(false);
        }

        String state = newState ? "&aON" : "&cOFF";
        String msg = "&#3BA8FFFly: &7" + state;
        if (!target.equals(sender)) {
            msg += " &8(" + target.getName() + ")";
        }
        sendRawMessage(sender, plugin.getConfigManager().getPrefix() + msg);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ConfigConstants.Permissions.FLY)) {
            return List.of();
        }

        if (args.length == 1) {
            return filterTabComplete(getOnlinePlayerNames(), args[0]);
        }

        return List.of();
    }
}

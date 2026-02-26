package xyz.lokili.loutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.util.ArrayList;
import java.util.List;

public class FlySpeedCommand extends CommandBase {

    public FlySpeedCommand(LoUtils plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!checkPermission(sender, ConfigConstants.Permissions.FLYSPEED)) {
            return true;
        }

        if (args.length < 1) {
            sendRawMessage(sender, plugin.getConfigManager().getPrefix() + 
                    "&cИспользование: /lflyspeed <0-10> [player]");
            return true;
        }

        int level;
        try {
            level = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sendMessage(sender, "invalid-number");
            return true;
        }

        if (level < 0 || level > 10) {
            sendRawMessage(sender, plugin.getConfigManager().getPrefix() + 
                    "&cСкорость должна быть от 0 до 10");
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
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

        // Bukkit fly speed range is 0.0 .. 1.0 (default 0.1)
        float speed = level / 10.0f;
        target.setFlySpeed(speed);

        String msg = "&#3BA8FFFlySpeed: &7" + level + "&8/10";
        if (!target.equals(sender)) {
            msg += " &8(" + target.getName() + ")";
        }
        sendRawMessage(sender, plugin.getConfigManager().getPrefix() + msg);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ConfigConstants.Permissions.FLYSPEED)) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            String start = args[0];
            for (int i = 0; i <= 10; i++) {
                String s = String.valueOf(i);
                if (s.startsWith(start)) list.add(s);
            }
            return list;
        }

        if (args.length == 2) {
            return filterTabComplete(getOnlinePlayerNames(), args[1]);
        }

        return List.of();
    }
}

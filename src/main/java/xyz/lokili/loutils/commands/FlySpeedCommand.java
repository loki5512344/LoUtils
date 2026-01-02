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

public class FlySpeedCommand implements CommandExecutor, TabCompleter {

    private final LoUtils plugin;

    public FlySpeedCommand(LoUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("loutils.flyspeed")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + "&cИспользование: /lflyspeed <0-10> [player]"));
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
            sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + "&cСкорость должна быть от 0 до 10"));
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
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command without a target");
                return true;
            }
            target = player;
        }

        // Bukkit fly speed range is 0.0 .. 1.0 (default 0.1)
        float speed = level / 10.0f;
        target.setFlySpeed(speed);

        String prefix = plugin.getConfigManager().getPrefix();
        String msg = prefix + "&#3BA8FFFlySpeed: &7" + level + "&8/10";
        if (!target.equals(sender)) {
            msg += " &8(" + target.getName() + ")";
        }
        sender.sendMessage(ColorUtil.colorize(msg));

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
        if (!sender.hasPermission("loutils.flyspeed")) {
            return new ArrayList<>();
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
            String start = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(start))
                    .limit(20)
                    .toList();
        }

        return new ArrayList<>();
    }
}

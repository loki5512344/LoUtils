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

public class FlyCommand implements CommandExecutor, TabCompleter {

    private final LoUtils plugin;

    public FlyCommand(LoUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("loutils.fly")) {
            sendMessage(sender, "no-permission");
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
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command without a target");
                return true;
            }
            target = player;
        }

        boolean newState = !target.getAllowFlight();
        target.setAllowFlight(newState);
        if (!newState) {
            target.setFlying(false);
        }

        String prefix = plugin.getConfigManager().getPrefix();
        String state = newState ? "&aON" : "&cOFF";
        String msg = prefix + "&#3BA8FFFly: &7" + state;
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
        if (!sender.hasPermission("loutils.fly")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String start = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(start))
                    .limit(20)
                    .toList();
        }

        return new ArrayList<>();
    }
}

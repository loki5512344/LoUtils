package xyz.lokili.loutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnMobCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    private final List<String> mobTypes;
    
    public SpawnMobCommand(LoUtils plugin) {
        this.plugin = plugin;
        this.mobTypes = Arrays.stream(EntityType.values())
                .filter(EntityType::isSpawnable)
                .filter(EntityType::isAlive)
                .map(e -> e.name().toLowerCase())
                .toList();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.spawnmob")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        
        if (args.length < 2) {
            sendMessage(sender, "spawnmob.usage");
            return true;
        }
        
        String mobName = args[0].toUpperCase();
        EntityType entityType;
        
        try {
            entityType = EntityType.valueOf(mobName);
        } catch (IllegalArgumentException e) {
            sendMessage(sender, "spawnmob.invalid-mob");
            return true;
        }
        
        if (!entityType.isSpawnable() || !entityType.isAlive()) {
            sendMessage(sender, "spawnmob.invalid-mob");
            return true;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if (amount < 1 || amount > 10000) {
                sendMessage(sender, "spawnmob.invalid-amount");
                return true;
            }
        } catch (NumberFormatException e) {
            sendMessage(sender, "spawnmob.invalid-amount");
            return true;
        }
        
        // Spawn mobs using region scheduler
        Bukkit.getRegionScheduler().execute(plugin, player.getLocation(), () -> {
            for (int i = 0; i < amount; i++) {
                player.getWorld().spawnEntity(player.getLocation(), entityType);
            }
        });
        
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage("spawnmob.spawned")
                .replace("{amount}", String.valueOf(amount))
                .replace("{mob}", mobName.toLowerCase());
        sender.sendMessage(ColorUtil.colorize(prefix + message));
        
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
        if (!sender.hasPermission("loutils.spawnmob")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return mobTypes.stream()
                    .filter(m -> m.startsWith(args[0].toLowerCase()))
                    .limit(20)
                    .toList();
        }
        
        if (args.length == 2) {
            return Arrays.asList("1", "10", "100", "1000", "10000");
        }
        
        return new ArrayList<>();
    }
}

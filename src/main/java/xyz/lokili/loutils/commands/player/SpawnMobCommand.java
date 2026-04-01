package xyz.lokili.loutils.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.util.Arrays;
import java.util.List;

public class SpawnMobCommand extends CommandBase {
    
    private final List<String> mobTypes = Arrays.stream(EntityType.values())
            .filter(e -> e.isSpawnable() && e.isAlive())
            .map(e -> e.name().toLowerCase())
            .toList();
    
    public SpawnMobCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, ConfigConstants.Permissions.SPAWNMOB)) return true;
        
        Player player = requirePlayer(sender);
        if (player == null) return true;
        
        if (args.length < 2) {
            sendMessage(sender, "spawnmob.usage");
            return true;
        }
        
        EntityType type;
        try {
            type = EntityType.valueOf(args[0].toUpperCase());
            if (!type.isSpawnable() || !type.isAlive()) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            sendMessage(sender, "spawnmob.invalid-mob");
            return true;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
            if (amount < 1 || amount > 10000) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sendMessage(sender, "spawnmob.invalid-amount");
            return true;
        }
        
        var loc = player.getLocation();
        Bukkit.getRegionScheduler().execute(plugin, loc, () -> {
            for (int i = 0; i < amount; i++) {
                player.getWorld().spawnEntity(loc, type);
            }
        });
        
        sendMessage(sender, "spawnmob.spawned", "{amount}", String.valueOf(amount), "{mob}", type.name().toLowerCase());
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ConfigConstants.Permissions.SPAWNMOB)) return List.of();
        
        if (args.length == 1) return filterTabComplete(mobTypes, args[0]);
        if (args.length == 2) return Arrays.asList("1", "10", "100", "1000");
        
        return List.of();
    }
}

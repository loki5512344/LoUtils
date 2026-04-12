package xyz.lokili.loutils.commands.gameplay;

import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;
import dev.lolib.utils.Colors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

public class EnchantCommand extends CommandBase {
    
    private final List<String> enchantNames = StreamSupport.stream(Registry.ENCHANTMENT.spliterator(), false)
            .map(e -> e.getKey().getKey()).toList();
    
    public EnchantCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, ConfigConstants.Permissions.ENCHANT)) return true;
        
        Player player = requirePlayer(sender);
        if (player == null) return true;
        
        if (args.length == 0) {
            msg(sender, "usage");
            return true;
        }
        
        // List enchantments
        if (args[0].equalsIgnoreCase("list")) {
            msg(sender, "list-header");
            StringBuilder sb = new StringBuilder("&7");
            for (int i = 0; i < enchantNames.size(); i++) {
                sb.append(enchantNames.get(i)).append(i % 5 == 4 ? "\n&7" : ", ");
            }
            sender.sendMessage(Colors.parse(sb.toString()));
            return true;
        }
        
        var item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            msg(sender, "no-item");
            return true;
        }
        
        // Clear all enchantments
        if (args[0].equalsIgnoreCase("clear")) {
            item.getEnchantments().keySet().forEach(item::removeEnchantment);
            msg(sender, "removed-all");
            return true;
        }
        
        // Add/remove enchantment
        return handleEnchant(sender, item, args);
    }
    
    private boolean handleEnchant(CommandSender sender, ItemStack item, String[] args) {
        if (args.length < 2) {
            msg(sender, "usage");
            return true;
        }
        
        var ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(args[0].toLowerCase()));
        if (ench == null) {
            msg(sender, "invalid-enchant", "{enchant}", args[0]);
            return true;
        }
        
        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            msg(sender, "invalid-level");
            return true;
        }
        
        var config = plugin.getConfigManager().getConfig(ConfigConstants.ENCHANT_CONFIG);
        int maxLevel = config.getInt("max_level", 0);
        
        if (maxLevel > 0 && level > maxLevel) {
            msg(sender, "max-level", "{max}", String.valueOf(maxLevel));
            return true;
        }
        
        if (level <= 0) {
            item.removeEnchantment(ench);
            msg(sender, "removed", "{enchant}", args[0]);
            return true;
        }
        
        var meta = item.getItemMeta();
        if (meta == null) {
            msg(sender, "no-item");
            return true;
        }
        
        meta.addEnchant(ench, level, config.getBoolean("allow_unsafe", true));
        item.setItemMeta(meta);
        msg(sender, "success", "{enchant}", args[0], "{level}", String.valueOf(level));
        return true;
    }
    
    private void msg(CommandSender sender, String key, String... replacements) {
        sendConfigMessage(sender, ConfigConstants.ENCHANT_CONFIG, "messages." + key, replacements);
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ConfigConstants.Permissions.ENCHANT)) return List.of();
        
        if (args.length == 1) {
            var list = new ArrayList<>(enchantNames);
            list.addAll(Arrays.asList("list", "clear"));
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).limit(20).toList();
        }
        
        if (args.length == 2 && !args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("clear")) {
            return Arrays.asList("1", "5", "10", "32", "100", "255");
        }
        
        return List.of();
    }
}

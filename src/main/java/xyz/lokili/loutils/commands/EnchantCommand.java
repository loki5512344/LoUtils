package xyz.lokili.loutils.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

public class EnchantCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    private final List<String> enchantNames;
    
    public EnchantCommand(LoUtils plugin) {
        this.plugin = plugin;
        this.enchantNames = StreamSupport.stream(Registry.ENCHANTMENT.spliterator(), false)
                .map(e -> e.getKey().getKey())
                .toList();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.enchant")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        
        if (args.length == 0) {
            sendConfigMessage(sender, "usage");
            return true;
        }
        
        // List enchantments
        if (args[0].equalsIgnoreCase("list")) {
            sendConfigMessage(sender, "list-header");
            StringBuilder sb = new StringBuilder("&7");
            int count = 0;
            for (String name : enchantNames) {
                sb.append(name);
                count++;
                if (count % 5 == 0) {
                    sender.sendMessage(ColorUtil.colorize(sb.toString()));
                    sb = new StringBuilder("&7");
                } else {
                    sb.append(", ");
                }
            }
            if (sb.length() > 2) {
                sender.sendMessage(ColorUtil.colorize(sb.toString()));
            }
            return true;
        }
        
        // Remove all enchantments
        if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("removeall")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                sendConfigMessage(sender, "no-item");
                return true;
            }
            
            for (Enchantment ench : item.getEnchantments().keySet()) {
                item.removeEnchantment(ench);
            }
            sendConfigMessage(sender, "removed-all");
            return true;
        }
        
        // Add/remove enchantment
        if (args.length < 2) {
            sendConfigMessage(sender, "usage");
            return true;
        }
        
        String enchantName = args[0].toLowerCase();
        Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantName));
        
        if (enchantment == null) {
            String msg = getConfigMessage("invalid-enchant").replace("{enchant}", enchantName);
            sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + msg));
            return true;
        }
        
        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendConfigMessage(sender, "invalid-level");
            return true;
        }
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            sendConfigMessage(sender, "no-item");
            return true;
        }
        
        FileConfiguration config = plugin.getConfigManager().getConfig("conf/enchant.yml");
        int maxLevel = config.getInt("max_level", 0);
        
        if (maxLevel > 0 && level > maxLevel) {
            String msg = getConfigMessage("max-level").replace("{max}", String.valueOf(maxLevel));
            sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + msg));
            return true;
        }
        
        // Remove enchantment if level is 0
        if (level <= 0) {
            item.removeEnchantment(enchantment);
            String msg = getConfigMessage("removed").replace("{enchant}", enchantName);
            sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + msg));
            return true;
        }
        
        // Add enchantment
        boolean allowUnsafe = config.getBoolean("allow_unsafe", true);
        
        if (allowUnsafe) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                sendConfigMessage(sender, "no-item");
                return true;
            }
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        } else {
            item.addEnchantment(enchantment, level);
        }
        
        String msg = getConfigMessage("success")
                .replace("{enchant}", enchantName)
                .replace("{level}", String.valueOf(level));
        sender.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + msg));
        
        return true;
    }
    
    private void sendMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    private void sendConfigMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = getConfigMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    private String getConfigMessage(String key) {
        FileConfiguration config = plugin.getConfigManager().getConfig("conf/enchant.yml");
        return config.getString("messages." + key, "Message not found: " + key);
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("loutils.enchant")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(enchantNames);
            suggestions.add("list");
            suggestions.add("clear");
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .limit(20)
                    .toList();
        }
        
        if (args.length == 2 && !args[0].equalsIgnoreCase("list") && !args[0].equalsIgnoreCase("clear")) {
            return Arrays.asList("1", "5", "10", "32", "100", "255", "1000");
        }
        
        return new ArrayList<>();
    }
}

package xyz.lokili.loutils.commands;

import dev.lolib.utils.Colors;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.util.ArrayList;
import java.util.List;

public class MapCommand implements CommandExecutor, TabCompleter {

    private final LoUtils plugin;
    private final NamespacedKey lockedKey;
    private final FileConfiguration config;

    public MapCommand(LoUtils plugin) {
        this.plugin = plugin;
        this.lockedKey = new NamespacedKey(plugin, "map_locked_by");
        this.config = plugin.getConfigManager().getConfig(ConfigConstants.MAP_LOCKING_CONFIG);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков");
            return true;
        }

        if (!config.getBoolean("enabled", true)) return true;

        if (args.length == 0) {
            player.sendMessage(Colors.parse("&#7858E9Использование: /map <lock|unlock>"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.FILLED_MAP) {
            player.sendActionBar(Colors.parse(config.getString("messages.not-map")));
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "lock" -> lockMap(player, item);
            case "unlock" -> unlockMap(player, item);
            default -> {
                player.sendMessage(Colors.parse("&#7858E9Использование: /map <lock|unlock>"));
                yield true;
            }
        };
    }

    private boolean lockMap(Player player, ItemStack map) {
        ItemMeta meta = map.getItemMeta();
        if (meta == null) return true;

        if (meta.getPersistentDataContainer().has(lockedKey, PersistentDataType.STRING)) {
            player.sendActionBar(Colors.parse(config.getString("messages.already-locked")));
            return true;
        }

        meta.getPersistentDataContainer().set(lockedKey, PersistentDataType.STRING, player.getUniqueId().toString());
        
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        String loreFormat = config.getString("lore-format", "&#B798A8Заблокировано: &#9878C9%player%");
        lore.add(Colors.parse(loreFormat.replace("%player%", player.getName())));
        meta.lore(lore);
        
        map.setItemMeta(meta);
        player.sendActionBar(Colors.parse(config.getString("messages.locked")));
        return true;
    }

    private boolean unlockMap(Player player, ItemStack map) {
        ItemMeta meta = map.getItemMeta();
        if (meta == null) return true;

        if (!meta.getPersistentDataContainer().has(lockedKey, PersistentDataType.STRING)) {
            player.sendActionBar(Colors.parse(config.getString("messages.not-locked")));
            return true;
        }

        String ownerUUID = meta.getPersistentDataContainer().get(lockedKey, PersistentDataType.STRING);
        if (ownerUUID == null || !ownerUUID.equals(player.getUniqueId().toString())) {
            player.sendActionBar(Colors.parse(config.getString("messages.not-owner")));
            return true;
        }

        meta.getPersistentDataContainer().remove(lockedKey);
        
        if (meta.hasLore()) {
            List<Component> lore = meta.lore();
            if (lore != null) {
                lore.removeIf(line -> net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(line).contains("Заблокировано"));
                meta.lore(lore.isEmpty() ? null : lore);
            }
        }
        
        map.setItemMeta(meta);
        player.sendActionBar(Colors.parse(config.getString("messages.unlocked")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("lock", "unlock");
        }
        return List.of();
    }
}

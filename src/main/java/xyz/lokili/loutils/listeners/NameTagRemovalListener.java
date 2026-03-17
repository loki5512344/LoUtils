package xyz.lokili.loutils.listeners;

import dev.lolib.utils.Colors;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

import java.util.List;

public class NameTagRemovalListener extends BaseListener {

    public NameTagRemovalListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.NAME_TAG_REMOVAL, ConfigConstants.NAME_TAG_REMOVAL_CONFIG);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!checkEnabled()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.SHEARS) return;

        Entity entity = event.getRightClicked();
        
        // Не работает на игроках
        if (entity.getType() == EntityType.PLAYER) return;

        // Проверка чёрного списка
        List<String> blacklist = config.getStringList("blacklist");
        if (blacklist.contains(entity.getType().name())) {
            player.sendActionBar(Colors.parse(config.getString("messages.blacklisted")));
            return;
        }

        // Проверка наличия имени
        if (entity.customName() == null) {
            player.sendActionBar(Colors.parse(config.getString("messages.no-tag")));
            return;
        }

        // Получаем имя моба
        String mobName = PlainTextComponentSerializer.plainText().serialize(entity.customName());

        // Дропаем бирку с именем
        if (config.getBoolean("drop-tag", true)) {
            ItemStack nameTag = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = nameTag.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(mobName);
                nameTag.setItemMeta(meta);
            }
            entity.getWorld().dropItemNaturally(entity.getLocation(), nameTag);
        }

        // Удаляем имя с моба
        entity.customName(null);
        entity.setCustomNameVisible(false);

        // Звук
        if (config.getBoolean("sounds", true)) {
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);
        }

        player.sendActionBar(Colors.parse(config.getString("messages.removed")));
        event.setCancelled(true);
    }
}

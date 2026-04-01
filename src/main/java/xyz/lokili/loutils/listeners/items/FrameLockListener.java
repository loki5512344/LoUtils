package xyz.lokili.loutils.listeners.items;

import dev.lolib.utils.Colors;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

public class FrameLockListener extends BaseListener {

    private final NamespacedKey lockedKey;

    public FrameLockListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, "frame-locking", ConfigConstants.FRAME_LOCKING_CONFIG);
        this.lockedKey = new NamespacedKey(plugin, "frame_locked");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFrameInteract(PlayerInteractEntityEvent event) {
        if (!checkEnabled()) return;
        if (!(event.getRightClicked() instanceof ItemFrame frame)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        boolean isLocked = frame.getPersistentDataContainer().has(lockedKey, PersistentDataType.BYTE);

        if (moduleConfig() == null) return;
        
        // Блокировка рамки мёдом
        String lockItemName = moduleConfig().getString("lock-item", "HONEYCOMB");
        if (item.getType().name().equals(lockItemName) && !isLocked) {
            frame.getPersistentDataContainer().set(lockedKey, PersistentDataType.BYTE, (byte) 1);
            
            if (moduleConfig().getBoolean("sounds", true)) {
                frame.getWorld().playSound(frame.getLocation(), Sound.ITEM_HONEYCOMB_WAX_ON, 1.0f, 1.0f);
            }
            if (moduleConfig().getBoolean("particles", true)) {
                frame.getWorld().spawnParticle(Particle.DRIPPING_HONEY, frame.getLocation(), 10, 0.2, 0.2, 0.2, 0);
            }
            
            player.sendActionBar(Colors.parse(moduleConfig().getString("messages.locked")));
            event.setCancelled(true);
            return;
        }

        // Разблокировка рамки кистью
        String unlockItemName = moduleConfig().getString("unlock-item", "BRUSH");
        if (item.getType().name().equals(unlockItemName) && isLocked) {
            frame.getPersistentDataContainer().remove(lockedKey);
            
            if (moduleConfig().getBoolean("sounds", true)) {
                frame.getWorld().playSound(frame.getLocation(), Sound.ITEM_AXE_WAX_OFF, 1.0f, 1.0f);
            }
            if (moduleConfig().getBoolean("particles", true)) {
                frame.getWorld().spawnParticle(Particle.WAX_OFF, frame.getLocation(), 10, 0.2, 0.2, 0.2, 0);
            }
            
            player.sendActionBar(Colors.parse(moduleConfig().getString("messages.unlocked")));
            event.setCancelled(true);
            return;
        }

        // Блокировка взаимодействия с заблокированной рамкой
        if (isLocked) {
            if (moduleConfig().getBoolean("prevent-rotation", true) || moduleConfig().getBoolean("prevent-item-removal", true)) {
                player.sendActionBar(Colors.parse(moduleConfig().getString("messages.is-locked")));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFrameDamage(EntityDamageByEntityEvent event) {
        if (!checkEnabled()) return;
        if (moduleConfig() == null || !moduleConfig().getBoolean("prevent-break", true)) return;
        
        if (event.getEntity().getType() != EntityType.ITEM_FRAME && 
            event.getEntity().getType() != EntityType.GLOW_ITEM_FRAME) return;

        ItemFrame frame = (ItemFrame) event.getEntity();
        if (!frame.getPersistentDataContainer().has(lockedKey, PersistentDataType.BYTE)) return;

        if (event.getDamager() instanceof Player player) {
            player.sendActionBar(Colors.parse(moduleConfig().getString("messages.is-locked")));
        }
        
        event.setCancelled(true);
    }
}

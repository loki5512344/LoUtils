package xyz.lokili.loutils.listeners;

import dev.lolib.scheduler.Scheduler;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;

/**
 * CauldronCrafting - Котел как станция переработки
 * 
 * Механики:
 * 1. Сухой бетон → Бетон (ПКМ или бросок)
 * 2. Стирка цветных предметов → белый цвет (ПКМ или бросок)
 */
public class CauldronListener implements Listener {
    
    private final LoUtils plugin;
    
    public CauldronListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.CAULDRON)) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.WATER_CAULDRON) return;
        
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;
        
        if (processConcrete(item, block, event.getPlayer()) || processWashing(item, block, null)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.CAULDRON)) return;
        
        Item droppedItem = event.getItemDrop();
        
        Scheduler.get(plugin).runLater(() -> {
            if (!droppedItem.isValid() || droppedItem.isDead()) return;
            
            Block block = droppedItem.getLocation().getBlock();
            if (block.getType() != Material.WATER_CAULDRON) return;
            
            ItemStack item = droppedItem.getItemStack();
            if (processConcrete(item, block, null) || processWashing(item, block, block)) {
                droppedItem.remove();
            }
        }, 1L);
    }
    
    /**
     * Обработка бетона: concrete powder → concrete
     * @param dropLocation если не null - дропаем предмет, иначе даём игроку
     */
    private boolean processConcrete(ItemStack item, Block cauldron, Player player) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.CAULDRON_CONFIG);
        if (!config.getBoolean("concrete-cleaning.enabled", true)) return false;
        if (!item.getType().name().endsWith("_CONCRETE_POWDER")) return false;
        
        int waterCost = config.getInt("concrete-cleaning.water-cost", 1);
        if (!hasWater(cauldron, waterCost)) return false;
        
        Material concrete = Material.getMaterial(item.getType().name().replace("_POWDER", ""));
        if (concrete == null) return false;
        
        ItemStack result = new ItemStack(concrete, item.getAmount());
        
        if (player != null) {
            item.setAmount(0);
            player.getInventory().addItem(result);
        } else {
            cauldron.getWorld().dropItemNaturally(cauldron.getLocation().add(0.5, 1.0, 0.5), result);
        }
        
        useWater(cauldron, waterCost);
        effect(cauldron, Sound.BLOCK_LAVA_EXTINGUISH, Particle.SPLASH, player != null ? 5 : 8);
        return true;
    }
    
    /**
     * Обработка стирки: цветной → белый
     * @param dropLocation если не null - дропаем предмет, иначе меняем in-place
     */
    private boolean processWashing(ItemStack item, Block cauldron, Block dropLocation) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.CAULDRON_CONFIG);
        if (!config.getBoolean("washing.enabled", true)) return false;
        
        String name = item.getType().name();
        if (!name.contains("LEATHER_") && !name.contains("BANNER") && !name.contains("BED")) return false;
        
        int waterCost = config.getInt("washing.water-cost", 1);
        if (!hasWater(cauldron, waterCost)) return false;
        
        ItemStack washed = dropLocation != null ? item.clone() : item;
        
        if (washed.getItemMeta() instanceof LeatherArmorMeta meta) {
            meta.setColor(null);
            washed.setItemMeta(meta);
        } else {
            String[] parts = name.split("_", 2);
            if (parts.length == 2) {
                Material white = Material.getMaterial("WHITE_" + parts[1]);
                if (white != null) washed.setType(white);
            }
        }
        
        if (dropLocation != null) {
            cauldron.getWorld().dropItemNaturally(cauldron.getLocation().add(0.5, 1.0, 0.5), washed);
        }
        
        useWater(cauldron, waterCost);
        effect(cauldron, Sound.ITEM_BUCKET_EMPTY, Particle.BUBBLE_POP, dropLocation != null ? 5 : 3);
        return true;
    }
    
    private boolean hasWater(Block cauldron, int required) {
        return cauldron.getBlockData() instanceof Levelled l && l.getLevel() >= required;
    }
    
    private void useWater(Block cauldron, int amount) {
        if (!(cauldron.getBlockData() instanceof Levelled l)) return;
        
        int newLevel = l.getLevel() - amount;
        if (newLevel <= 0) {
            cauldron.setType(Material.CAULDRON);
        } else {
            l.setLevel(newLevel);
            cauldron.setBlockData(l);
        }
    }
    
    private void effect(Block cauldron, Sound sound, Particle particle, int count) {
        var loc = cauldron.getLocation().add(0.5, 0.5, 0.5);
        cauldron.getWorld().playSound(loc, sound, 1.0f, 1.0f);
        cauldron.getWorld().spawnParticle(particle, loc, count, 0.3, 0.3, 0.3);
    }
}

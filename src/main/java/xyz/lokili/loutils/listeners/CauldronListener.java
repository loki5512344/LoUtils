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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.constants.GameplayConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.services.EffectService;

/**
 * CauldronCrafting - Котел как станция переработки
 * 
 * Механики:
 * 1. Сухой бетон → Бетон (ПКМ или бросок)
 * 2. Стирка цветных предметов → белый цвет (ПКМ или бросок)
 */
public class CauldronListener extends BaseListener {
    
    private final EffectService effectService;
    
    public CauldronListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CAULDRON, ConfigConstants.CAULDRON_CONFIG);
        this.effectService = plugin.getContainer().getEffectService();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!checkEnabled()) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.WATER_CAULDRON) return;
        
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;
        
        if (processConcrete(item, block, event.getPlayer()) || processWashing(item, block, event.getPlayer(), false)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!checkEnabled()) return;
        
        Item droppedItem = event.getItemDrop();
        
        // Используем location-based scheduler вместо глобального
        Scheduler.get(plugin).runLaterAtLocation(droppedItem.getLocation(), () -> {
            if (!droppedItem.isValid() || droppedItem.isDead()) return;
            
            // Проверяем блок под предметом (где он приземлится)
            Block block = droppedItem.getLocation().subtract(0, 0.5, 0).getBlock();
            if (block.getType() != Material.WATER_CAULDRON) return;
            
            ItemStack item = droppedItem.getItemStack();
            if (processConcrete(item, block, null) || processWashing(item, block, null, true)) {
                droppedItem.remove();
            }
        }, 5L);
    }
    
    /**
     * Обработка бетона: concrete powder → concrete
     * @param dropLocation если не null - дропаем предмет, иначе даём игроку
     */
    private boolean processConcrete(ItemStack item, Block cauldron, Player player) {
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
        effectService.playBlockEffect(cauldron, Sound.BLOCK_LAVA_EXTINGUISH, Particle.SPLASH, player != null ? 5 : 8);
        return true;
    }
    
    /**
     * Обработка стирки: цветной → белый
     * @param player игрок (может быть null для броска)
     * @param isDropped true если предмет брошен, false если ПКМ
     */
    private boolean processWashing(ItemStack item, Block cauldron, Player player, boolean isDropped) {
        if (!config.getBoolean("washing.enabled", true)) return false;
        
        String name = item.getType().name();
        if (!name.contains("LEATHER_") && !name.contains("BANNER") && !name.contains("BED")) return false;
        
        int waterCost = config.getInt("washing.water-cost", 1);
        if (!hasWater(cauldron, waterCost)) return false;
        
        if (item.getItemMeta() instanceof LeatherArmorMeta meta) {
            meta.setColor(null);
            item.setItemMeta(meta);
        } else {
            String[] parts = name.split("_", 2);
            if (parts.length == 2) {
                Material white = Material.getMaterial("WHITE_" + parts[1]);
                if (white != null) {
                    ItemStack newItem = new ItemStack(white, item.getAmount());
                    if (item.hasItemMeta()) {
                        newItem.setItemMeta(item.getItemMeta());
                    }
                    
                    if (isDropped) {
                        // Для броска - дропаем новый предмет
                        cauldron.getWorld().dropItemNaturally(cauldron.getLocation().add(0.5, 1.0, 0.5), newItem);
                    } else if (player != null) {
                        // Для ПКМ - заменяем в руке игрока
                        player.getInventory().setItemInMainHand(newItem);
                    }
                }
            }
        }
        
        useWater(cauldron, waterCost);
        effectService.playBlockEffect(cauldron, Sound.ITEM_BUCKET_EMPTY, Particle.BUBBLE_POP, isDropped ? 5 : 3);
        return true;
    }
    
    private boolean hasWater(Block cauldron, int required) {
        return cauldron.getBlockData() instanceof Levelled l && l.getLevel() >= required;
    }
    
    private void useWater(Block cauldron, int amount) {
        if (!(cauldron.getBlockData() instanceof Levelled l)) return;
        
        int newLevel = l.getLevel() - amount;
        if (newLevel <= GameplayConstants.CAULDRON_EMPTY_LEVEL) {
            cauldron.setType(Material.CAULDRON);
        } else {
            l.setLevel(newLevel);
            cauldron.setBlockData(l);
        }
    }
}

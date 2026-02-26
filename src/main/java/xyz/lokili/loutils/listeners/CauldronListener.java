package xyz.lokili.loutils.listeners;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;

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
        
        boolean handled = handleConcrete(item, block) || handleWashing(item, block);
        if (handled) event.setCancelled(true);
    }
    
    private boolean handleConcrete(ItemStack item, Block cauldron) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.CAULDRON_CONFIG);
        if (!config.getBoolean("concrete-cleaning.enabled", true)) return false;
        if (!item.getType().name().endsWith("_CONCRETE_POWDER")) return false;
        
        int waterCost = config.getInt("concrete-cleaning.water-cost", 1);
        if (!hasWater(cauldron, waterCost)) return false;
        
        Material concrete = Material.getMaterial(item.getType().name().replace("_POWDER", ""));
        if (concrete == null) return false;
        
        item.setType(concrete);
        useWater(cauldron, waterCost);
        effect(cauldron, Sound.BLOCK_LAVA_EXTINGUISH, Particle.SPLASH, 5);
        return true;
    }
    
    private boolean handleWashing(ItemStack item, Block cauldron) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.CAULDRON_CONFIG);
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
                if (white != null) item.setType(white);
            }
        }
        
        useWater(cauldron, waterCost);
        effect(cauldron, Sound.ITEM_BUCKET_EMPTY, Particle.BUBBLE_POP, 3);
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

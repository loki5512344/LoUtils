package xyz.lokili.loutils.listeners.items;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

import java.util.Random;

public class EnhancedBoneMealListener extends BaseListener {

    private final Random random = new Random();

    public EnhancedBoneMealListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, "enhanced-bone-meal", ConfigConstants.ENHANCED_BONE_MEAL_CONFIG);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBoneMealUse(PlayerInteractEvent event) {
        if (!checkEnabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BONE_MEAL) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (moduleConfig() == null) return;
        
        Material type = block.getType();
        boolean success = false;

        if (type == Material.SUGAR_CANE && moduleConfig().getBoolean("sugar-cane.enabled", true)) {
            success = growPlant(block, "sugar-cane");
        } else if (type == Material.CACTUS && moduleConfig().getBoolean("cactus.enabled", true)) {
            success = growPlant(block, "cactus");
        }

        if (success) {
            event.setCancelled(true);
            
            // Уменьшаем количество костной муки
            if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    private boolean growPlant(Block block, String plantType) {
        double chance = moduleConfig().getDouble(plantType + ".success-chance", 0.75);
        if (random.nextDouble() > chance) return false;

        int maxHeight = moduleConfig().getInt(plantType + ".max-height", 3);
        int currentHeight = getPlantHeight(block);
        
        if (currentHeight >= maxHeight) return false;

        int minGrowth = moduleConfig().getInt(plantType + ".growth-amount-min", 1);
        int maxGrowth = moduleConfig().getInt(plantType + ".growth-amount-max", 3);
        int growthAmount = minGrowth + random.nextInt(maxGrowth - minGrowth + 1);

        // Находим верхний блок растения
        Block topBlock = block;
        while (topBlock.getRelative(BlockFace.UP).getType() == block.getType()) {
            topBlock = topBlock.getRelative(BlockFace.UP);
        }

        // Выращиваем растение
        Material plantMaterial = block.getType();
        for (int i = 0; i < growthAmount; i++) {
            if (currentHeight + i >= maxHeight) break;
            
            Block nextBlock = topBlock.getRelative(BlockFace.UP);
            if (nextBlock.getType() != Material.AIR) break;
            
            nextBlock.setType(plantMaterial);
            topBlock = nextBlock;
        }

        // Эффекты
        if (moduleConfig().getBoolean("particles", true)) {
            block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, 
                topBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0);
        }
        if (moduleConfig().getBoolean("sounds", true)) {
            block.getWorld().playSound(topBlock.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
        }

        return true;
    }

    private int getPlantHeight(Block block) {
        Material type = block.getType();
        int height = 1;

        // Считаем вниз
        Block below = block.getRelative(BlockFace.DOWN);
        while (below.getType() == type) {
            height++;
            below = below.getRelative(BlockFace.DOWN);
        }

        // Считаем вверх
        Block above = block.getRelative(BlockFace.UP);
        while (above.getType() == type) {
            height++;
            above = above.getRelative(BlockFace.UP);
        }

        return height;
    }
}

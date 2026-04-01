package xyz.lokili.loutils.listeners.items;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

import java.util.*;

public class EnhancedHoeListener extends BaseListener {

    private final Random random = new Random();
    private final Map<Material, Material> cropToSeed = new HashMap<>();

    public EnhancedHoeListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, "enhanced-hoes", ConfigConstants.ENHANCED_HOES_CONFIG);
        initializeCropMapping();
    }

    private void initializeCropMapping() {
        cropToSeed.put(Material.WHEAT, Material.WHEAT_SEEDS);
        cropToSeed.put(Material.CARROTS, Material.CARROT);
        cropToSeed.put(Material.POTATOES, Material.POTATO);
        cropToSeed.put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
        cropToSeed.put(Material.NETHER_WART, Material.NETHER_WART);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCropBreak(BlockBreakEvent event) {
        if (!checkEnabled()) return;

        Block block = event.getBlock();
        if (!isMatureCrop(block)) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        if (!isHoe(tool.getType())) return;
        if (moduleConfig() == null) return;

        int bonusDrops = calculateBonusDrops(tool.getType());
        if (bonusDrops <= 0) return;

        // Получаем дроп материал
        Material dropMaterial = getDropMaterial(block.getType());
        if (dropMaterial == null) return;

        // Добавляем бонусный дроп
        block.getWorld().dropItemNaturally(
            block.getLocation().add(0.5, 0.5, 0.5),
            new ItemStack(dropMaterial, bonusDrops)
        );
    }

    private boolean isMatureCrop(Block block) {
        if (!(block.getBlockData() instanceof Ageable ageable)) return false;
        return ageable.getAge() == ageable.getMaximumAge();
    }

    private boolean isHoe(Material material) {
        return material.name().endsWith("_HOE");
    }

    private int calculateBonusDrops(Material hoeMaterial) {
        String hoeType = hoeMaterial.name().replace("_HOE", "").toLowerCase();
        
        int min = moduleConfig().getInt("hoes." + hoeType + ".min-bonus", 0);
        int max = moduleConfig().getInt("hoes." + hoeType + ".max-bonus", 0);
        
        if (min <= 0 || max <= 0) return 0;
        
        return min + random.nextInt(max - min + 1);
    }

    private Material getDropMaterial(Material cropType) {
        return switch (cropType) {
            case WHEAT -> Material.WHEAT;
            case CARROTS -> Material.CARROT;
            case POTATOES -> Material.POTATO;
            case BEETROOTS -> Material.BEETROOT;
            case NETHER_WART -> Material.NETHER_WART;
            default -> null;
        };
    }
}

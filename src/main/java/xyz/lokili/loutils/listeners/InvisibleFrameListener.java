package xyz.lokili.loutils.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;

/**
 * InvisibleFrameListener - Невидимые рамки
 * 
 * Механики:
 * 1. Кинуть splash зелье невидимости на рамку на стене
 * 2. Крафт: 1 зелье невидимости (центр) + 8 рамок = 8 невидимых рамок
 */
public class InvisibleFrameListener implements Listener {
    
    private final LoUtils plugin;
    private final NamespacedKey invisibleKey;
    
    public InvisibleFrameListener(LoUtils plugin) {
        this.plugin = plugin;
        this.invisibleKey = new NamespacedKey(plugin, "invisible_frame");
        registerRecipe();
    }
    
    /**
     * Регистрация крафта: 1 зелье невидимости + 8 рамок = 8 невидимых рамок
     */
    private void registerRecipe() {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.INVISIBLE_FRAMES_CONFIG);
        if (!config.getBoolean("crafting-method", true)) return;
        
        ItemStack result = createInvisibleFrame();
        result.setAmount(8);
        
        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(plugin, "invisible_frame"),
            result
        );
        
        recipe.shape("FFF", "FPF", "FFF");
        recipe.setIngredient('F', Material.ITEM_FRAME);
        recipe.setIngredient('P', Material.POTION); // Зелье невидимости проверяется в PrepareItemCraftEvent
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException e) {
            // Recipe already exists
        }
    }
    
    /**
     * Создание невидимой рамки
     */
    private ItemStack createInvisibleFrame() {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.INVISIBLE_FRAMES_CONFIG);
        
        ItemStack item = new ItemStack(Material.ITEM_FRAME);
        ItemMeta meta = item.getItemMeta();
        
        // Маркер невидимой рамки
        meta.getPersistentDataContainer().set(invisibleKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
        
        // CustomModelData
        int customModelData = config.getInt("custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Проверка крафта - зелье должно быть зельем невидимости
     */
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.INVISIBLE_FRAMES_CONFIG);
        if (!config.getBoolean("crafting-method", true)) return;
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.INVISIBLE_FRAMES)) return;
        
        if (event.getRecipe() == null || event.getRecipe().getResult().getType() != Material.ITEM_FRAME) return;
        
        ItemStack[] matrix = event.getInventory().getMatrix();
        if (matrix.length < 9) return;
        
        // Проверяем центр - зелье невидимости
        ItemStack center = matrix[4];
        if (!isInvisibilityPotion(center)) {
            event.getInventory().setResult(null);
            return;
        }
        
        // Проверяем что вокруг 8 рамок
        int frameCount = 0;
        for (int i = 0; i < matrix.length; i++) {
            if (i != 4 && matrix[i] != null && matrix[i].getType() == Material.ITEM_FRAME) {
                frameCount++;
            }
        }
        
        if (frameCount == 8) {
            ItemStack result = createInvisibleFrame();
            result.setAmount(8);
            event.getInventory().setResult(result);
        } else {
            event.getInventory().setResult(null);
        }
    }
    
    /**
     * Splash зелье невидимости попало в рамку
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.INVISIBLE_FRAMES_CONFIG);
        if (!config.getBoolean("splash-potion-method", true)) return;
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.INVISIBLE_FRAMES)) return;
        
        if (!(event.getEntity() instanceof ThrownPotion potion)) return;
        if (event.getHitEntity() == null || event.getHitEntity().getType() != EntityType.ITEM_FRAME) return;
        
        if (isInvisibilityPotion(potion.getItem(), true)) {
            ((ItemFrame) event.getHitEntity()).setVisible(false);
        }
    }
    
    private boolean isInvisibilityPotion(ItemStack item) {
        return isInvisibilityPotion(item, false);
    }
    
    private boolean isInvisibilityPotion(ItemStack item, boolean splash) {
        if (item == null) return false;
        if (splash && item.getType() != Material.SPLASH_POTION) return false;
        if (!splash && item.getType() != Material.POTION) return false;
        
        return item.getItemMeta() instanceof PotionMeta meta 
            && meta.getBasePotionType() == PotionType.INVISIBILITY;
    }
}

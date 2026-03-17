package xyz.lokili.loutils.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

/**
 * InvisibleFrameListener - Невидимые рамки
 * 
 * Механики:
 * 1. Кинуть splash зелье невидимости на рамку на стене
 * 2. Крафт: 1 зелье невидимости (центр) + 8 рамок = 8 невидимых рамок
 */
public class InvisibleFrameListener extends BaseListener {

    private final NamespacedKey invisibleKey;

    public InvisibleFrameListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.INVISIBLE_FRAMES, ConfigConstants.INVISIBLE_FRAMES_CONFIG);
        this.invisibleKey = new NamespacedKey(plugin, "invisible_frame");
        registerRecipe();
    }

    /**
     * Регистрация крафта: 1 зелье невидимости + 8 рамок = 8 невидимых рамок
     */
    private void registerRecipe() {
        if (!config.getBoolean("crafting-method", true)) return;

        ItemStack result = createInvisibleFrame();
        result.setAmount(8);

        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(plugin, "invisible_frame"),
            result
        );

        recipe.shape("FFF", "FPF", "FFF");
        recipe.setIngredient('F', Material.ITEM_FRAME);
        recipe.setIngredient('P', Material.POTION);

        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException e) {
            // Recipe already exists
        }
    }

    /**
     * Создание невидимой рамки с названием
     */
    private ItemStack createInvisibleFrame() {
        ItemStack item = new ItemStack(Material.ITEM_FRAME);
        ItemMeta meta = item.getItemMeta();

        // Сохраняем NBT тег
        meta.getPersistentDataContainer().set(invisibleKey, PersistentDataType.BYTE, (byte) 1);

        // Название из конфига
        String displayName = config.getString("display-name", "§fНевидимая рамка");
        meta.displayName(dev.lolib.utils.Colors.parse(displayName));

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
        if (!config.getBoolean("crafting-method", true)) return;
        if (!checkEnabled()) return;

        if (event.getRecipe() == null || event.getRecipe().getResult().getType() != Material.ITEM_FRAME) return;

        ItemStack[] matrix = event.getInventory().getMatrix();
        if (matrix.length < 9) return;

        ItemStack center = matrix[4];
        if (!isInvisibilityPotion(center)) {
            event.getInventory().setResult(null);
            return;
        }

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
        if (!config.getBoolean("splash-potion-method", true)) return;
        if (!checkEnabled()) return;

        if (!(event.getEntity() instanceof ThrownPotion potion)) return;
        if (event.getHitEntity() == null || event.getHitEntity().getType() != EntityType.ITEM_FRAME) return;

        if (isInvisibilityPotion(potion.getItem(), true)) {
            ((ItemFrame) event.getHitEntity()).setVisible(false);
        }
    }
    
    /**
     * При установке рамки - проверяем NBT и делаем невидимой
     */
    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!checkEnabled()) return;
        
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        
        // Получаем предмет из руки игрока
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.ITEM_FRAME) {
            item = player.getInventory().getItemInOffHand();
        }
        
        if (item.getType() != Material.ITEM_FRAME || !item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE)) return;
        
        // Делаем рамку невидимой
        frame.setVisible(false);
        
        plugin.getLogger().info("[InvisibleFrame] Placed invisible frame at " + 
            frame.getLocation().getBlockX() + "," + 
            frame.getLocation().getBlockY() + "," + 
            frame.getLocation().getBlockZ());
    }

    /**
     * Проверка: является ли предмет зельем невидимости (любого типа)
     */
    private boolean isInvisibilityPotion(ItemStack item) {
        return isInvisibilityPotion(item, false);
    }

    /**
     * Проверка: является ли предмет зельем невидимости
     * @param item предмет для проверки
     * @param splashOnly только splash зелья (для броска)
     */
    private boolean isInvisibilityPotion(ItemStack item, boolean splashOnly) {
        if (item == null) return false;
        
        // Поддержка всех типов зелий невидимости
        Material type = item.getType();
        if (splashOnly) {
            // Для броска: splash или lingering
            if (type != Material.SPLASH_POTION && type != Material.LINGERING_POTION) {
                return false;
            }
        } else {
            // Для крафта: любое зелье невидимости
            if (type != Material.POTION && type != Material.SPLASH_POTION && type != Material.LINGERING_POTION) {
                return false;
            }
        }

        return item.getItemMeta() instanceof PotionMeta meta
            && meta.getBasePotionType() == PotionType.INVISIBILITY;
    }
}

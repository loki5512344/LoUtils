package xyz.lokili.loutils.listeners.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import dev.lolib.scheduler.Scheduler;
import org.bukkit.Bukkit;
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
        Scheduler.get(plugin).runLater(this::registerInvisibleFrameRecipe, 1L);
        startParticleTask();
    }

    /**
     * Регистрация крафта: 1 зелье невидимости + 8 рамок = 8 невидимых рамок
     */
    public void registerInvisibleFrameRecipe() {
        if (moduleConfig() == null || !moduleConfig().getBoolean("crafting-method", true)) return;

        Bukkit.removeRecipe(new NamespacedKey(plugin, "invisible_frame"));

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
        String displayName = moduleConfig().getString("display-name", "§fНевидимая рамка");
        meta.displayName(dev.lolib.utils.Colors.parse(displayName));

        int customModelData = moduleConfig().getInt("custom-model-data", 0);
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
        if (moduleConfig() == null || !moduleConfig().getBoolean("crafting-method", true)) return;
        if (!checkEnabled()) return;

        var recipe = event.getRecipe();
        if (recipe == null) return;
        ItemStack recipeOut = recipe.getResult();
        if (recipeOut == null || recipeOut.getType() != Material.ITEM_FRAME) return;

        var inv = event.getInventory();
        if (inv == null) return;

        ItemStack[] matrix = inv.getMatrix();
        if (matrix == null || matrix.length < 9) return;

        ItemStack center = matrix[4];
        if (!isInvisibilityPotion(center)) {
            inv.setResult(null);
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
            inv.setResult(result);
        } else {
            inv.setResult(null);
        }
    }

    /**
     * Splash зелье невидимости попало в рамку
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!moduleConfig().getBoolean("splash-potion-method", true)) return;
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
    
    /**
     * Дроп невидимой рамки при разрушении
     */
    @EventHandler
    public void onFrameBreak(HangingBreakByEntityEvent event) {
        if (!checkEnabled()) return;
        if (moduleConfig() == null) return;
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        if (!frame.isVisible()) {
            // Рамка невидимая - дропаем невидимую рамку
            event.setCancelled(true);
            frame.remove();
            
            // Дропаем предмет из рамки
            ItemStack item = frame.getItem();
            if (item != null && item.getType() != Material.AIR) {
                frame.getWorld().dropItemNaturally(frame.getLocation(), item);
            }
            
            // Дропаем невидимую рамку
            frame.getWorld().dropItemNaturally(frame.getLocation(), createInvisibleFrame());
        }
    }
    
    /**
     * Альтернативный обработчик для разрушения рамки
     */
    @EventHandler
    public void onFrameDamage(EntityDamageByEntityEvent event) {
        if (!checkEnabled()) return;
        if (moduleConfig() == null) return;
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        
        if (!frame.isVisible() && event.getDamager() instanceof Player) {
            // Рамка невидимая - дропаем невидимую рамку
            event.setCancelled(true);
            
            // Дропаем предмет из рамки
            ItemStack item = frame.getItem();
            if (item != null && item.getType() != Material.AIR) {
                frame.getWorld().dropItemNaturally(frame.getLocation(), item);
            }
            
            // Дропаем невидимую рамку
            frame.getWorld().dropItemNaturally(frame.getLocation(), createInvisibleFrame());
            frame.remove();
        }
    }
    
    /**
     * Показ частиц вокруг невидимых рамок когда держишь невидимую рамку в руке
     */
    private void startParticleTask() {
        if (moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("show-particles", true)) return;
        
        int radius = moduleConfig().getInt("particle-radius", 16);
        
        dev.lolib.scheduler.Scheduler.get(plugin).runTimer(() -> {
            if (moduleConfig() == null || !moduleConfig().getBoolean("show-particles", true)) return;
            
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                // Запускаем задачу в регионе игрока
                dev.lolib.scheduler.Scheduler.get(plugin).runAtEntity(player, () -> {
                    // Проверяем, держит ли игрок невидимую рамку
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    
                    boolean holdingInvisibleFrame = isInvisibleFrame(mainHand) || isInvisibleFrame(offHand);
                    if (!holdingInvisibleFrame) return;
                    
                    // Показываем частицы вокруг всех невидимых рамок в радиусе
                    Location playerLoc = player.getLocation();
                    player.getWorld().getNearbyEntities(playerLoc, radius, radius, radius).forEach(entity -> {
                        if (entity instanceof ItemFrame frame && !frame.isVisible()) {
                            showFrameParticles(frame, player);
                        }
                    });
                });
            }
        }, 10L, 10L); // Каждые 0.5 секунды
    }
    
    /**
     * Показать частицы вокруг рамки (8 углов)
     */
    private void showFrameParticles(ItemFrame frame, Player player) {
        Location loc = frame.getLocation();
        
        // 8 углов рамки (как у блока света)
        double[][] offsets = {
            {0.1, 0.1, 0.1}, {0.9, 0.1, 0.1},
            {0.1, 0.9, 0.1}, {0.9, 0.9, 0.1},
            {0.1, 0.1, 0.9}, {0.9, 0.1, 0.9},
            {0.1, 0.9, 0.9}, {0.9, 0.9, 0.9}
        };
        
        for (double[] offset : offsets) {
            Location particleLoc = loc.clone().add(offset[0] - 0.5, offset[1] - 0.5, offset[2] - 0.5);
            player.spawnParticle(Particle.GLOW, particleLoc, 1, 0, 0, 0, 0);
        }
    }
    
    /**
     * Проверка: является ли предмет невидимой рамкой
     */
    private boolean isInvisibleFrame(ItemStack item) {
        if (item == null || item.getType() != Material.ITEM_FRAME) return false;
        if (!item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE);
    }
}

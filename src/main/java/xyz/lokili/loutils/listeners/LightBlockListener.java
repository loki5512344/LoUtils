package xyz.lokili.loutils.listeners;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;

/**
 * LightBlockListener - Источник света
 * 
 * Механики:
 * 1. Крафт: 4 светокамня вокруг + 1 свечка в центре = блок света уровня 15
 * 2. Shift + ПКМ: изменить уровень света 0-15 (на блоке или в руке)
 * 3. Shift + ЛКМ: сломать блок и получить дроп с сохраненным уровнем
 */
public class LightBlockListener implements Listener {
    
    private final LoUtils plugin;
    private final NamespacedKey lightLevelKey;
    
    public LightBlockListener(LoUtils plugin) {
        this.plugin = plugin;
        this.lightLevelKey = new NamespacedKey(plugin, "light_level");
        registerRecipe();
    }
    
    /**
     * Регистрация крафта: 4 светокамня + 1 свечка = блок света
     */
    private void registerRecipe() {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.LIGHT_BLOCK_CONFIG);
        if (!config.getBoolean("crafting-enabled", true)) return;
        
        ItemStack result = createLightBlock(15);
        
        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(plugin, "light_block"),
            result
        );
        
        recipe.shape("GGG", "GCG", "GGG");
        recipe.setIngredient('G', Material.GLOWSTONE);
        recipe.setIngredient('C', Material.CANDLE);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException e) {
            // Recipe already exists
        }
    }
    
    /**
     * Создание блока света с уровнем
     */
    private ItemStack createLightBlock(int level) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.LIGHT_BLOCK_CONFIG);
        
        ItemStack item = new ItemStack(Material.LIGHT);
        ItemMeta meta = item.getItemMeta();
        
        // Сохраняем уровень
        meta.getPersistentDataContainer().set(lightLevelKey, PersistentDataType.INTEGER, level);
        
        // Название
        String nameFormat = config.getString("name-format", "§eИсточник света §7[§f%level%§7]");
        meta.displayName(Colors.parse(nameFormat.replace("%level%", String.valueOf(level))));
        
        // CustomModelData
        int customModelData = config.getInt("custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Shift + ПКМ: изменить уровень света
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.LIGHT_BLOCK)) return;
        if (!event.getPlayer().isSneaking()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Block block = event.getClickedBlock();
        
        // Изменение уровня блока света в мире
        if (block != null && block.getType() == Material.LIGHT) {
            changeLightLevel(block);
            event.setCancelled(true);
            return;
        }
        
        // Изменение уровня блока света в руке
        if (item.getType() == Material.LIGHT && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().has(lightLevelKey, PersistentDataType.INTEGER)) {
                int currentLevel = meta.getPersistentDataContainer().get(lightLevelKey, PersistentDataType.INTEGER);
                int newLevel = getNextLevel(currentLevel);
                
                ItemStack newItem = createLightBlock(newLevel);
                player.getInventory().setItemInMainHand(newItem);
                event.setCancelled(true);
            }
        }
    }
    
    /**
     * Изменить уровень света блока в мире
     */
    private void changeLightLevel(Block block) {
        if (!(block.getBlockData() instanceof Light light)) return;
        
        int currentLevel = light.getLevel();
        int newLevel = getNextLevel(currentLevel);
        
        light.setLevel(newLevel);
        block.setBlockData(light);
    }
    
    /**
     * Получить следующий уровень света (циклично 0-15)
     */
    private int getNextLevel(int current) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.LIGHT_BLOCK_CONFIG);
        int maxLevel = config.getInt("max-level", 15);
        int minLevel = config.getInt("min-level", 0);
        
        int next = current + 1;
        if (next > maxLevel) {
            return minLevel;
        }
        return next;
    }
    
    /**
     * Shift + ЛКМ: сломать блок и получить дроп с уровнем
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.LIGHT_BLOCK)) return;
        
        Block block = event.getBlock();
        if (block.getType() != Material.LIGHT) return;
        
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        
        // Получаем уровень света
        if (!(block.getBlockData() instanceof Light light)) return;
        int level = light.getLevel();
        
        // Отменяем стандартный дроп
        event.setDropItems(false);
        
        // Дропаем блок света с сохраненным уровнем
        ItemStack drop = createLightBlock(level);
        block.getWorld().dropItemNaturally(block.getLocation(), drop);
    }
}

package xyz.lokili.loutils.listeners.blocks;

import dev.lolib.scheduler.Scheduler;
import dev.lolib.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.services.LightParticleService;

/**
 * LightBlockListener - Источник света
 * 
 * Механики:
 * 1. Крафт: 4 светокамня + 1 свечка = блок света уровня 15
 * 2. Shift + ПКМ: изменить уровень света 0-15
 * 3. ЛКМ: сломать блок и получить дроп с сохраненным уровнем
 * 4. Частицы: показываются когда игрок держит блок света в руке
 */
public class LightBlockListener extends BaseListener {
    
    private final NamespacedKey lightLevelKey;
    private final LightParticleService particleService;
    
    public LightBlockListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager, LightParticleService particleService) {
        super(plugin, configManager, ConfigConstants.Modules.LIGHT_BLOCK, ConfigConstants.LIGHT_BLOCK_CONFIG);
        this.lightLevelKey = new NamespacedKey(plugin, "light_level");
        this.particleService = particleService;
        Scheduler.get(plugin).runLater(this::registerLightBlockRecipe, 1L);
    }
    
    public void registerLightBlockRecipe() {
        if (moduleConfig() == null || !moduleConfig().getBoolean("crafting-enabled", true)) return;

        Bukkit.removeRecipe(new NamespacedKey(plugin, "light_block"));
        
        ItemStack result = createLightBlock(15);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "light_block"), result);
        recipe.shape("GGG", "GCG", "GGG");
        recipe.setIngredient('G', Material.GLOWSTONE);
        recipe.setIngredient('C', Material.CANDLE);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    private ItemStack createLightBlock(int level) {
        ItemStack item = new ItemStack(Material.LIGHT);
        ItemMeta meta = item.getItemMeta();
        
        meta.getPersistentDataContainer().set(lightLevelKey, PersistentDataType.INTEGER, level);
        
        String nameFormat = moduleConfig().getString("name-format", "§eИсточник света §7[§f%level%§7]");
        meta.displayName(Colors.parse(nameFormat.replace("%level%", String.valueOf(level))));
        
        int customModelData = moduleConfig().getInt("custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * ЛКМ по блоку света: сломать и получить дроп
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeftClick(PlayerInteractEvent event) {
        if (!checkEnabled()) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.LIGHT) return;
        
        Player player = event.getPlayer();
        if (!(block.getBlockData() instanceof Light light)) return;
        
        int level = light.getLevel();
        event.setCancelled(true);
        
        block.setType(Material.AIR);
        ItemStack drop = createLightBlock(level);
        block.getWorld().dropItemNaturally(block.getLocation(), drop);
        particleService.remove(block.getLocation());
        
        player.sendMessage(Colors.parse("§eБлок света сломан (уровень §f" + level + "§e)"));
    }
    
    /**
     * ПКМ: изменить уровень света
     * - На блоке (Shift + ПКМ): изменить уровень установленного блока
     * - В воздух (Shift + ПКМ): изменить уровень предмета в руке
     */
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (!checkEnabled()) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Shift + ПКМ по блоку света
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.LIGHT) {
                if (!(block.getBlockData() instanceof Light light)) return;
                
                int level = light.getLevel();
                level++;
                if (level > 15) level = 0;
                
                light.setLevel(level);
                block.setBlockData(light, false);
                
                player.sendMessage(Colors.parse("§eУровень света: §f" + level));
                event.setCancelled(true);
                return;
            }
        }
        
        // Shift + ПКМ в воздух с блоком света в руке
        if (event.getAction() == Action.RIGHT_CLICK_AIR && player.isSneaking()) {
            if (item.getType() == Material.LIGHT && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(lightLevelKey, PersistentDataType.INTEGER)) {
                    int currentLevel = meta.getPersistentDataContainer().get(lightLevelKey, PersistentDataType.INTEGER);
                    int newLevel = currentLevel + 1;
                    if (newLevel > 15) newLevel = 0;
                    
                    ItemStack newItem = createLightBlock(newLevel);
                    player.getInventory().setItemInMainHand(newItem);
                    player.sendMessage(Colors.parse("§eУровень света: §f" + newLevel));
                    event.setCancelled(true);
                }
            }
        }
    }
    
    /**
     * Установка блока света: устанавливаем правильный уровень из NBT
     */
    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!checkEnabled()) return;
        
        Block block = event.getBlockPlaced();
        if (block.getType() != Material.LIGHT) return;
        
        ItemStack item = event.getItemInHand();
        if (!item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(lightLevelKey, PersistentDataType.INTEGER)) return;
        
        int level = meta.getPersistentDataContainer().get(lightLevelKey, PersistentDataType.INTEGER);
        
        if (block.getBlockData() instanceof Light light) {
            light.setLevel(level);
            block.setBlockData(light, false);
        }
        
        particleService.add(block.getLocation());
    }
}

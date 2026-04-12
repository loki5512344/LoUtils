package xyz.lokili.loutils.listeners.crafts;

import dev.lolib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

/**
 * CustomCraftsListener - Регистрация кастомных крафтов
 * 
 * Крафты:
 * 1. Колокол (bell)
 * 2. Красный гриб блок (red mushroom block)
 * 3. Коричневый гриб блок (brown mushroom block)
 * 4. Паутина (cobweb)
 * 5. Подмостки x4 (scaffolding)
 * 6. Фейерверк 4 уровня (firework level 4)
 * 7. Кварц x8: 8 андезита вокруг булыжника (3x3)
 * (Элитры — см. ElytraCraftListener)
 */
public class CustomCraftsListener extends BaseListener {
    
    public CustomCraftsListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CUSTOM_CRAFTS, ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        // После полной загрузки сервера (Paper 1.21+), иначе addRecipe иногда не применяется
        Scheduler.get(plugin).runLater(this::registerCrafts, 1L);
    }

    /** Регистрация рецептов; вызывается с задержкой при старте и при {@code /loutils reload}. */
    public void registerCrafts() {
        if (!checkEnabled()) return;
        if (moduleConfig() == null) {
            plugin.getLogger().warning("Custom crafts config not loaded!");
            return;
        }

        removePluginCraftRecipes();
        
        if (moduleConfig().getBoolean("bell.enabled", true)) {
            registerBellCraft();
        }
        
        if (moduleConfig().getBoolean("red-mushroom-block.enabled", true)) {
            registerRedMushroomBlockCraft();
        }
        
        if (moduleConfig().getBoolean("brown-mushroom-block.enabled", true)) {
            registerBrownMushroomBlockCraft();
        }
        
        if (moduleConfig().getBoolean("cobweb.enabled", true)) {
            registerCobwebCraft();
        }
        
        if (moduleConfig().getBoolean("scaffolding.enabled", true)) {
            registerScaffoldingCraft();
        }

        if (moduleConfig().getBoolean("quartz.enabled", true)) {
            registerQuartzCraft();
        }
    }

    private void removePluginCraftRecipes() {
        String[] keys = {"bell", "red_mushroom_block", "brown_mushroom_block", "cobweb", "scaffolding", "custom_quartz"};
        for (String id : keys) {
            Bukkit.removeRecipe(new NamespacedKey(plugin, id));
        }
    }
    
    /**
     * Колокол: золото x3 / камень + палка + камень / самородок x3
     */
    private void registerBellCraft() {
        ItemStack result = new ItemStack(Material.BELL);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "bell"), result);
        
        recipe.shape("GGG", "STS", "III");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('S', Material.STONE);
        recipe.setIngredient('T', Material.STICK);
        recipe.setIngredient('I', Material.GOLD_NUGGET);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    /**
     * Красный гриб блок: 4 красных гриба
     */
    private void registerRedMushroomBlockCraft() {
        ItemStack result = new ItemStack(Material.RED_MUSHROOM_BLOCK);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "red_mushroom_block"), result);
        
        recipe.shape("MM", "MM");
        recipe.setIngredient('M', Material.RED_MUSHROOM);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    /**
     * Коричневый гриб блок: 4 коричневых гриба
     */
    private void registerBrownMushroomBlockCraft() {
        ItemStack result = new ItemStack(Material.BROWN_MUSHROOM_BLOCK);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "brown_mushroom_block"), result);
        
        recipe.shape("MM", "MM");
        recipe.setIngredient('M', Material.BROWN_MUSHROOM);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    /**
     * Паутина: 5 ниток
     */
    private void registerCobwebCraft() {
        ItemStack result = new ItemStack(Material.COBWEB);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "cobweb"), result);
        
        recipe.shape("S S", " S ", "S S");
        recipe.setIngredient('S', Material.STRING);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    /**
     * Подмостки x4: 6 палок + нить
     */
    private void registerScaffoldingCraft() {
        if (moduleConfig() == null) return;
        int amount = moduleConfig().getInt("scaffolding.amount", 4);
        ItemStack result = new ItemStack(Material.SCAFFOLDING, amount);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "scaffolding"), result);
        
        recipe.shape("TNT", "T T", "T T");
        recipe.setIngredient('T', Material.STICK);
        recipe.setIngredient('N', Material.STRING);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }

    /**
     * Кварц: верстак 3×3 — булыжник по центру, андезит по периметру (8 шт.) → кварц x amount.
     * Перед добавлением старый рецепт с тем же ключом удаляется — иначе после /reload крафт ломается.
     */
    private void registerQuartzCraft() {
        if (moduleConfig() == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "custom_quartz");

        int amount = moduleConfig().getInt("quartz.amount", 8);
        ItemStack result = new ItemStack(Material.QUARTZ, amount);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape("AAA", "ACA", "AAA");
        recipe.setIngredient('A', Material.ANDESITE);
        recipe.setIngredient('C', Material.COBBLESTONE);
        recipe.setCategory(CraftingBookCategory.MISC);

        if (!Bukkit.addRecipe(recipe)) {
            plugin.getLogger().warning("Не удалось зарегистрировать рецепт custom_quartz (кварц). Проверьте конфликт ключей.");
        }
    }
}

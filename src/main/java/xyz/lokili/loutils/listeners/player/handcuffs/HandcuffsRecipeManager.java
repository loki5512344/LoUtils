package xyz.lokili.loutils.listeners.player.handcuffs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import xyz.lokili.loutils.LoUtils;

/**
 * Управление рецептом крафта кандалов
 */
public class HandcuffsRecipeManager {

    private final LoUtils plugin;
    private final NamespacedKey itemKey;

    public HandcuffsRecipeManager(LoUtils plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "handcuffs_item");
    }

    public NamespacedKey getItemKey() {
        return itemKey;
    }

    /**
     * Регистрация рецепта крафта кандалов
     */
    public void registerHandcuffsRecipe(YamlConfiguration config) {
        if (config == null || !config.getBoolean("crafting-enabled", true)) {
            return;
        }

        Bukkit.removeRecipe(new NamespacedKey(plugin, "handcuffs"));

        YamlConfiguration emptyConfig = new YamlConfiguration();
        ItemStack result = plugin.getContainer().getItemFactory()
            .createHandcuffs(config != null ? config : emptyConfig);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "handcuffs"), result);
        recipe.shape("NNN", "NIN", "NNN");
        recipe.setIngredient('N', Material.IRON_NUGGET);
        recipe.setIngredient('I', Material.IRON_INGOT);

        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
}

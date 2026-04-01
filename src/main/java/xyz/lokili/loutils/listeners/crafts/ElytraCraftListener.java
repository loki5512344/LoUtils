package xyz.lokili.loutils.listeners.crafts;

import dev.lolib.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
/**
 * Крафт кастомных элитр (имя, лор, половина прочности, без зачарований).
 * Рецепт: слизь — алмаз — слизь / мембрана — алмазный нагрудник — мембрана.
 */
public class ElytraCraftListener extends BaseListener {

    /** Ванильная максимальная прочность элитр (Java). */
    private static final int VANILLA_ELYTRA_MAX_DAMAGE = 432;

    public ElytraCraftListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CUSTOM_CRAFTS, ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        registerElytraCraft();
    }

    private void registerElytraCraft() {
        if (!checkEnabled()) return;
        if (moduleConfig() == null) {
            plugin.getLogger().warning("Custom crafts config not loaded for elytra!");
            return;
        }
        if (!moduleConfig().getBoolean("elytra.enabled", true)) return;

        NamespacedKey key = new NamespacedKey(plugin, "elytra_craft");
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, createCustomElytra());
        recipe.shape("SDS", "PGP", "   ");
        recipe.setIngredient('S', Material.SLIME_BALL);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('P', Material.PHANTOM_MEMBRANE);
        recipe.setIngredient('G', Material.DIAMOND_CHESTPLATE);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        if (!Bukkit.addRecipe(recipe)) {
            plugin.getLogger().warning("Не удалось зарегистрировать рецепт elytra_craft.");
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (!checkEnabled()) return;
        if (moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("elytra.enabled", true)) return;

        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType() != Material.ELYTRA) return;

        ItemStack[] matrix = event.getInventory().getMatrix();
        if (!isElytraCraft(matrix)) return;

        event.getInventory().setResult(createCustomElytra());
    }

    private boolean isElytraCraft(ItemStack[] matrix) {
        if (matrix == null || matrix.length < 9) return false;
        return matches(matrix[0], Material.SLIME_BALL)
                && matches(matrix[1], Material.DIAMOND)
                && matches(matrix[2], Material.SLIME_BALL)
                && matches(matrix[3], Material.PHANTOM_MEMBRANE)
                && matches(matrix[4], Material.DIAMOND_CHESTPLATE)
                && matches(matrix[5], Material.PHANTOM_MEMBRANE)
                && empty(matrix[6])
                && empty(matrix[7])
                && empty(matrix[8]);
    }

    private boolean matches(ItemStack stack, Material material) {
        return stack != null && stack.getType() == material;
    }

    private boolean empty(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR;
    }

    private ItemStack createCustomElytra() {
        if (moduleConfig() == null) return new ItemStack(Material.ELYTRA);

        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        if (meta == null) return elytra;

        int maxDamage = moduleConfig().getInt("elytra.max-damage", VANILLA_ELYTRA_MAX_DAMAGE / 2);
        if (maxDamage < 1) maxDamage = VANILLA_ELYTRA_MAX_DAMAGE / 2;
        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(maxDamage);
            damageable.setDamage(0);
        }

        String nameFormat = moduleConfig().getString("elytra.name-format", "&cКрылья фантома");
        meta.displayName(Colors.parse(nameFormat));

        elytra.setItemMeta(meta);
        CustomElytraHelper.markAndInit(plugin, elytra);
        CustomElytraHelper.stripAllEnchants(elytra);
        CustomElytraHelper.applyElytraLore(plugin, elytra, moduleConfig());
        return elytra;
    }
}

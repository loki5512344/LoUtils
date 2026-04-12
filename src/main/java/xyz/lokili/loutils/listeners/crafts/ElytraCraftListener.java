package xyz.lokili.loutils.listeners.crafts;

import dev.lolib.utils.Colors;
import dev.lolib.scheduler.Scheduler;
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
 * Базовый рецепт: слизь — алмаз — слизь / мембрана — алмазный нагрудник — мембрана.
 * Уровень 2: заряд ветра — алмазный блок — заряд ветра / алмазный блок — элитра tier1 — алмазный блок / ...
 */
public class ElytraCraftListener extends BaseListener {

    private static final int DEFAULT_CUSTOM_ELYTRA_MAX_DAMAGE = 500;

    public ElytraCraftListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CUSTOM_CRAFTS, ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        Scheduler.get(plugin).runLater(this::registerElytraCraft, 1L);
    }

    public void registerElytraCraft() {
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

        registerElytraTier2Upgrade();
    }

    private void registerElytraTier2Upgrade() {
        if (moduleConfig() == null || !moduleConfig().getBoolean("elytra.enabled", true)) return;
        if (!moduleConfig().getBoolean("elytra.tier2.enabled", true)) return;

        NamespacedKey key = new NamespacedKey(plugin, "elytra_upgrade_t2");
        Bukkit.removeRecipe(key);

        ItemStack dummy = new ItemStack(Material.ELYTRA);
        ShapedRecipe recipe = new ShapedRecipe(key, dummy);
        recipe.shape("WBW", "BEB", "WBW");
        recipe.setIngredient('W', Material.WIND_CHARGE);
        recipe.setIngredient('B', Material.DIAMOND_BLOCK);
        recipe.setIngredient('E', Material.ELYTRA);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        if (!Bukkit.addRecipe(recipe)) {
            plugin.getLogger().warning("Не удалось зарегистрировать рецепт elytra_upgrade_t2.");
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (!checkEnabled()) return;
        if (moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("elytra.enabled", true)) return;

        var inv = event.getInventory();
        if (inv == null) return;

        ItemStack result = inv.getResult();
        if (result == null || result.getType() != Material.ELYTRA) return;

        ItemStack[] matrix = inv.getMatrix();
        if (isElytraTier2Upgrade(matrix)) {
            ItemStack center = matrix[4];
            ItemStack upgraded = CustomElytraHelper.createTier2FromTier1(plugin, center, moduleConfig());
            inv.setResult(upgraded != null ? upgraded : null);
            return;
        }

        if (!isElytraCraft(matrix)) return;

        inv.setResult(createCustomElytra());
    }

    private boolean isElytraTier2Upgrade(ItemStack[] matrix) {
        if (matrix == null || matrix.length < 9) return false;
        return matches(matrix[0], Material.WIND_CHARGE)
                && matches(matrix[1], Material.DIAMOND_BLOCK)
                && matches(matrix[2], Material.WIND_CHARGE)
                && matches(matrix[3], Material.DIAMOND_BLOCK)
                && matches(matrix[5], Material.DIAMOND_BLOCK)
                && matches(matrix[6], Material.WIND_CHARGE)
                && matches(matrix[7], Material.DIAMOND_BLOCK)
                && matches(matrix[8], Material.WIND_CHARGE)
                && CustomElytraHelper.isCustomElytra(plugin, matrix[4])
                && CustomElytraHelper.getElytraTier(plugin, matrix[4]) == 1;
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

        int maxDamage = moduleConfig().getInt("elytra.max-damage", DEFAULT_CUSTOM_ELYTRA_MAX_DAMAGE);
        if (maxDamage < 1) maxDamage = DEFAULT_CUSTOM_ELYTRA_MAX_DAMAGE;
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

package xyz.lokili.loutils.listeners.crafts.elytra;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import xyz.lokili.loutils.constants.ElytraConstants;

/**
 * Сервис для апгрейда и обновления элитр
 * Single Responsibility: только операции апгрейда/обновления
 */
public class ElytraUpgradeService {

    private final Plugin plugin;
    private final ElytraDataManager dataManager;
    private final ElytraEnchantmentManager enchantmentManager;
    private final ElytraLoreFormatter loreFormatter;

    public ElytraUpgradeService(Plugin plugin, ElytraDataManager dataManager,
                                ElytraEnchantmentManager enchantmentManager,
                                ElytraLoreFormatter loreFormatter) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.enchantmentManager = enchantmentManager;
        this.loreFormatter = loreFormatter;
    }

    /**
     * Обновляет старые элитры: маркер, прочность, имя, чары, лор
     */
    public boolean upgradeLegacyElytra(ItemStack stack, FileConfiguration cfg) {
        if (stack == null || stack.getType() != Material.ELYTRA || cfg == null) {
            return false;
        }

        if (!cfg.getBoolean("elytra.enabled", true)) {
            return false;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Инициализируем PDC если нужно
        if (!dataManager.isCustomItem(stack)) {
            dataManager.initializeElytra(stack);
        }

        // Обновляем прочность
        int tier = dataManager.getElytraTier(stack);
        int maxDamage = getMaxDamageForTier(tier, cfg);

        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(maxDamage);
        }

        // Обновляем имя
        String nameFormat = getNameFormatForTier(tier, cfg);
        meta.displayName(Colors.parse(nameFormat));

        stack.setItemMeta(meta);

        // Чистим запрещённые чары
        enchantmentManager.stripForbiddenEnchants(stack);

        // Обновляем лор
        loreFormatter.applyElytraLore(stack, cfg);

        return true;
    }

    /**
     * Прокачка tier 1 → tier 2
     * Сохраняет полёт/перезарядку из PDC, обновляет прочность и имя
     */
    public ItemStack createTier2FromTier1(ItemStack tier1, FileConfiguration cfg) {
        if (tier1 == null || tier1.getType() != Material.ELYTRA || cfg == null) {
            return null;
        }

        if (!cfg.getBoolean("elytra.enabled", true) || !cfg.getBoolean("elytra.tier2.enabled", true)) {
            return null;
        }

        if (!dataManager.isCustomItem(tier1) || dataManager.getElytraTier(tier1) != ElytraConstants.TIER_1) {
            return null;
        }

        ItemStack tier2 = tier1.clone();
        ItemMeta meta = tier2.getItemMeta();
        if (meta == null) {
            return null;
        }

        // Обновляем прочность
        int maxDamage = cfg.getInt("elytra.tier2.max-damage", ElytraConstants.DEFAULT_TIER2_MAX_DAMAGE);
        if (maxDamage < 1) {
            maxDamage = ElytraConstants.DEFAULT_TIER2_MAX_DAMAGE;
        }

        if (meta instanceof Damageable damageable) {
            int currentDamage = damageable.getDamage();
            damageable.setMaxDamage(maxDamage);
            damageable.setDamage(Math.min(currentDamage, maxDamage));
        }

        // Устанавливаем tier 2
        dataManager.setElytraTier(tier2, ElytraConstants.TIER_2);

        // Обновляем имя
        String nameFormat = cfg.getString("elytra.tier2.name-format", ElytraConstants.DEFAULT_TIER2_NAME_FORMAT);
        meta.displayName(Colors.parse(nameFormat));

        tier2.setItemMeta(meta);

        // Чистим запрещённые чары
        enchantmentManager.stripForbiddenEnchants(tier2);

        // Обновляем лор
        loreFormatter.applyElytraLore(tier2, cfg);

        return tier2;
    }

    /**
     * Получить максимальную прочность для тира
     */
    private int getMaxDamageForTier(int tier, FileConfiguration cfg) {
        if (tier >= ElytraConstants.TIER_2 && cfg.getBoolean("elytra.tier2.enabled", true)) {
            int value = cfg.getInt("elytra.tier2.max-damage", ElytraConstants.DEFAULT_TIER2_MAX_DAMAGE);
            return value < 1 ? ElytraConstants.DEFAULT_TIER2_MAX_DAMAGE : value;
        }

        int value = cfg.getInt("elytra.max-damage", ElytraConstants.DEFAULT_MAX_DAMAGE);
        return value < 1 ? ElytraConstants.DEFAULT_MAX_DAMAGE : value;
    }

    /**
     * Получить формат имени для тира
     */
    private String getNameFormatForTier(int tier, FileConfiguration cfg) {
        if (tier >= ElytraConstants.TIER_2 && cfg.getBoolean("elytra.tier2.enabled", true)) {
            return cfg.getString("elytra.tier2.name-format", ElytraConstants.DEFAULT_TIER2_NAME_FORMAT);
        }

        return cfg.getString("elytra.name-format", ElytraConstants.DEFAULT_NAME_FORMAT);
    }
}

package xyz.lokili.loutils.listeners.crafts;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.lokili.loutils.listeners.crafts.elytra.ElytraDataManager;
import xyz.lokili.loutils.listeners.crafts.elytra.ElytraEnchantmentManager;
import xyz.lokili.loutils.listeners.crafts.elytra.ElytraLoreFormatter;
import xyz.lokili.loutils.listeners.crafts.elytra.ElytraUpgradeService;

/**
 * Фасад для работы с кастомными элитрами
 * Делегирует работу специализированным классам (SOLID - Single Responsibility)
 *
 * @deprecated Используйте напрямую ElytraDataManager, ElytraEnchantmentManager, ElytraLoreFormatter, ElytraUpgradeService
 */
@Deprecated
public final class CustomElytraHelper {

    private CustomElytraHelper() {}

    private static ElytraDataManager dataManager;
    private static ElytraEnchantmentManager enchantmentManager;
    private static ElytraLoreFormatter loreFormatter;
    private static ElytraUpgradeService upgradeService;

    /**
     * Инициализация (вызывается один раз при старте плагина)
     */
    public static void initialize(Plugin plugin) {
        dataManager = new ElytraDataManager(plugin);
        enchantmentManager = new ElytraEnchantmentManager();
        loreFormatter = new ElytraLoreFormatter(dataManager);
        upgradeService = new ElytraUpgradeService(plugin, dataManager, enchantmentManager, loreFormatter);
    }

    // === Delegation to ElytraDataManager ===

    public static boolean isCustomElytra(Plugin plugin, ItemStack stack) {
        return dataManager.isCustomItem(stack);
    }

    public static long getRechargeUntilMs(Plugin plugin, ItemStack stack) {
        return dataManager.getRechargeUntilMs(stack);
    }

    public static boolean isRecharging(Plugin plugin, ItemStack stack) {
        return dataManager.isRecharging(stack);
    }

    public static long getFlightTicks(Plugin plugin, ItemStack stack) {
        return dataManager.getFlightTicks(stack);
    }

    public static void setFlightTicks(Plugin plugin, ItemStack stack, long ticks) {
        dataManager.setFlightTicks(stack, ticks);
    }

    public static void setRechargeUntil(Plugin plugin, ItemStack stack, long epochMs) {
        dataManager.setRechargeUntil(stack, epochMs);
    }

    public static void clearRechargeIfDone(Plugin plugin, ItemStack stack) {
        dataManager.clearRechargeIfDone(stack);
    }

    public static int getElytraTier(Plugin plugin, ItemStack stack) {
        return dataManager.getElytraTier(stack);
    }

    public static void setElytraTier(Plugin plugin, ItemStack stack, int tier) {
        dataManager.setElytraTier(stack, tier);
    }

    public static void markAndInit(Plugin plugin, ItemStack stack) {
        dataManager.initializeElytra(stack);
    }

    public static int getEffectiveFlightLimitTicks(Plugin plugin, ItemStack stack, FileConfiguration cfg) {
        return dataManager.getEffectiveFlightLimitTicks(stack, cfg);
    }

    public static int getEffectiveRechargeMs(Plugin plugin, ItemStack stack, FileConfiguration cfg) {
        return dataManager.getEffectiveRechargeMs(stack, cfg);
    }

    // === Delegation to ElytraEnchantmentManager ===

    public static void stripAllEnchants(ItemStack stack) {
        enchantmentManager.stripAllEnchants(stack);
    }

    public static void stripForbiddenEnchants(ItemStack stack) {
        enchantmentManager.stripForbiddenEnchants(stack);
    }

    public static boolean hasForbiddenEnchants(ItemStack stack) {
        return enchantmentManager.hasForbiddenEnchants(stack);
    }

    // === Delegation to ElytraLoreFormatter ===

    public static void applyElytraLore(Plugin plugin, ItemStack stack, FileConfiguration cfg) {
        loreFormatter.applyElytraLore(stack, cfg);
    }

    // === Delegation to ElytraUpgradeService ===

    public static boolean upgradeLegacyElytra(Plugin plugin, ItemStack stack, FileConfiguration cfg) {
        return upgradeService.upgradeLegacyElytra(stack, cfg);
    }

    public static ItemStack createTier2FromTier1(Plugin plugin, ItemStack tier1, FileConfiguration cfg) {
        return upgradeService.createTier2FromTier1(tier1, cfg);
    }

    // === Direct access to managers (recommended) ===

    public static ElytraDataManager getDataManager() {
        return dataManager;
    }

    public static ElytraEnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }

    public static ElytraLoreFormatter getLoreFormatter() {
        return loreFormatter;
    }

    public static ElytraUpgradeService getUpgradeService() {
        return upgradeService;
    }
}

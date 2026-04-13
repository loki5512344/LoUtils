package xyz.lokili.loutils.listeners.crafts.elytra;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import xyz.lokili.loutils.constants.ElytraConstants;
import xyz.lokili.loutils.utils.items.CustomItemHelper;

/**
 * Управление данными кастомных элитр через PDC
 * Single Responsibility: только работа с данными
 */
public class ElytraDataManager extends CustomItemHelper {

    private final NamespacedKey keyRechargeUntil;
    private final NamespacedKey keyFlightTicks;
    private final NamespacedKey keyElytraTier;

    public ElytraDataManager(Plugin plugin) {
        super(plugin, "custom_crafted_elytra", Material.ELYTRA);
        this.keyRechargeUntil = new NamespacedKey(plugin, "elytra_recharge_until_ms");
        this.keyFlightTicks = new NamespacedKey(plugin, "elytra_flight_ticks");
        this.keyElytraTier = new NamespacedKey(plugin, "elytra_tier");
    }

    // === Recharge ===

    public long getRechargeUntilMs(ItemStack stack) {
        return getPDCValue(stack, keyRechargeUntil, PersistentDataType.LONG, 0L);
    }

    public void setRechargeUntil(ItemStack stack, long epochMs) {
        if (epochMs <= 0L) {
            removePDCValue(stack, keyRechargeUntil);
        } else {
            setPDCValue(stack, keyRechargeUntil, PersistentDataType.LONG, epochMs);
        }
    }

    public boolean isRecharging(ItemStack stack) {
        long until = getRechargeUntilMs(stack);
        return until > 0L && System.currentTimeMillis() < until;
    }

    public void clearRechargeIfDone(ItemStack stack) {
        long until = getRechargeUntilMs(stack);
        if (until > 0L && System.currentTimeMillis() >= until) {
            setRechargeUntil(stack, 0L);
            setFlightTicks(stack, 0L);
        }
    }

    // === Flight Ticks ===

    public long getFlightTicks(ItemStack stack) {
        return getPDCValue(stack, keyFlightTicks, PersistentDataType.LONG, 0L);
    }

    public void setFlightTicks(ItemStack stack, long ticks) {
        setPDCValue(stack, keyFlightTicks, PersistentDataType.LONG, ticks);
    }

    // === Tier ===

    public int getElytraTier(ItemStack stack) {
        if (!isCustomItem(stack)) {
            return 0;
        }
        int tier = getPDCValue(stack, keyElytraTier, PersistentDataType.BYTE, (byte) ElytraConstants.TIER_1).intValue();
        return Math.max(ElytraConstants.MIN_TIER, Math.min(ElytraConstants.MAX_TIER, tier));
    }

    public void setElytraTier(ItemStack stack, int tier) {
        int clampedTier = Math.max(ElytraConstants.MIN_TIER, Math.min(ElytraConstants.MAX_TIER, tier));
        setPDCValue(stack, keyElytraTier, PersistentDataType.BYTE, (byte) clampedTier);
    }

    // === Initialization ===

    public void initializeElytra(ItemStack stack) {
        markAsCustom(stack);
        setElytraTier(stack, ElytraConstants.TIER_1);
        removePDCValue(stack, keyRechargeUntil);
        removePDCValue(stack, keyFlightTicks);
    }

    // === Config-based values ===

    public int getEffectiveFlightLimitTicks(ItemStack stack, FileConfiguration cfg) {
        if (cfg == null) {
            return ElytraConstants.DEFAULT_FLIGHT_LIMIT_TICKS;
        }

        int tier = getElytraTier(stack);
        if (tier >= ElytraConstants.TIER_2 && cfg.getBoolean("elytra.tier2.enabled", true)) {
            int value = cfg.getInt("elytra.tier2.flight-limit-ticks", ElytraConstants.DEFAULT_TIER2_FLIGHT_LIMIT_TICKS);
            return value < 1 ? ElytraConstants.DEFAULT_TIER2_FLIGHT_LIMIT_TICKS : value;
        }

        int value = cfg.getInt("elytra.flight-limit-ticks", ElytraConstants.DEFAULT_FLIGHT_LIMIT_TICKS);
        return value < 1 ? ElytraConstants.DEFAULT_FLIGHT_LIMIT_TICKS : value;
    }

    public int getEffectiveRechargeMs(ItemStack stack, FileConfiguration cfg) {
        if (cfg == null) {
            return ElytraConstants.DEFAULT_RECHARGE_SECONDS * ElytraConstants.MS_PER_SECOND;
        }

        int tier = getElytraTier(stack);
        if (tier >= ElytraConstants.TIER_2 && cfg.getBoolean("elytra.tier2.enabled", true)) {
            int seconds = cfg.getInt("elytra.tier2.recharge-seconds", ElytraConstants.DEFAULT_TIER2_RECHARGE_SECONDS);
            return (seconds < 1 ? ElytraConstants.DEFAULT_TIER2_RECHARGE_SECONDS : seconds) * ElytraConstants.MS_PER_SECOND;
        }

        int seconds = cfg.getInt("elytra.recharge-seconds", ElytraConstants.DEFAULT_RECHARGE_SECONDS);
        return (seconds < 1 ? ElytraConstants.DEFAULT_RECHARGE_SECONDS : seconds) * ElytraConstants.MS_PER_SECOND;
    }
}

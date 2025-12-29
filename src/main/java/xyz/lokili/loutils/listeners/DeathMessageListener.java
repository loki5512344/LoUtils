package xyz.lokili.loutils.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

public class DeathMessageListener implements Listener {
    
    private final LoUtils plugin;
    
    public DeathMessageListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().getDeathMessagesConfig().getBoolean("enabled", true)) {
            return;
        }
        
        if (!plugin.getConfigManager().getDeathMessagesConfig().getBoolean("disable_vanilla", true)) {
            return;
        }
        
        Player victim = event.getEntity();
        String victimName = victim.getName();
        
        String message = getDeathMessage(victim);
        
        event.deathMessage(ColorUtil.colorize(message));
    }
    
    private String getDeathMessage(Player victim) {
        String victimName = victim.getName();
        Player killer = victim.getKiller();
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        
        // PvP death
        if (killer != null) {
            // Check if killer is invisible
            if (killer.hasPotionEffect(PotionEffectType.INVISIBILITY) || 
                plugin.getVanishManager().isVanished(killer)) {
                return plugin.getConfigManager().getRandomDeathMessage("invisible_killer",
                        "{victim}", victimName);
            }
            
            // Get weapon type
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            String weaponType = getWeaponType(weapon);
            
            return plugin.getConfigManager().getRandomDeathMessage("pvp." + weaponType,
                    "{victim}", victimName,
                    "{killer}", killer.getName());
        }
        
        // Environment death
        if (lastDamage != null) {
            EntityDamageEvent.DamageCause cause = lastDamage.getCause();
            
            // Check for mob kill
            if (lastDamage instanceof EntityDamageByEntityEvent entityDamage) {
                Entity damager = entityDamage.getDamager();
                if (!(damager instanceof Player)) {
                    String mobType = damager.getType().name().toLowerCase();
                    String path = "mobs." + mobType;
                    
                    // Check if specific mob message exists
                    if (plugin.getConfigManager().getDeathMessagesConfig().contains(path)) {
                        return plugin.getConfigManager().getRandomDeathMessage(path,
                                "{victim}", victimName);
                    }
                    return plugin.getConfigManager().getRandomDeathMessage("mobs.default",
                            "{victim}", victimName);
                }
            }
            
            String envPath = getEnvironmentPath(cause);
            if (envPath != null) {
                return plugin.getConfigManager().getRandomDeathMessage("environment." + envPath,
                        "{victim}", victimName);
            }
        }
        
        // Default death
        return plugin.getConfigManager().getRandomDeathMessage("other",
                "{victim}", victimName);
    }
    
    private String getWeaponType(ItemStack weapon) {
        if (weapon == null || weapon.getType() == Material.AIR) {
            return "fist";
        }
        
        String name = weapon.getType().name().toLowerCase();
        
        if (name.contains("sword")) return "sword";
        if (name.contains("axe")) return "axe";
        if (name.contains("bow") || name.contains("crossbow")) return "bow";
        if (name.contains("trident")) return "trident";
        
        return "other";
    }
    
    private String getEnvironmentPath(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case FALL -> "fall";
            case LAVA -> "lava";
            case FIRE, FIRE_TICK -> "fire";
            case DROWNING -> "drown";
            case VOID -> "void";
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> "explosion";
            case CONTACT -> "cactus";
            case LIGHTNING -> "lightning";
            case STARVATION -> "starve";
            case POISON -> "poison";
            case WITHER -> "wither";
            case MAGIC -> "magic";
            default -> null;
        };
    }
}

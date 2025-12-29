package xyz.lokili.loutils.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.*;
import xyz.lokili.loutils.LoUtils;

public class VanishListener implements Listener {
    
    private final LoUtils plugin;
    
    public VanishListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Handle vanish on join
        plugin.getVanishManager().handleJoin(player);
        
        // Silent join for vanished players
        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getVanishManager().getConfig().getBoolean("silent_join_quit", true)) {
                event.joinMessage(null);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Silent quit for vanished players
        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getVanishManager().getConfig().getBoolean("silent_join_quit", true)) {
                event.quitMessage(null);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        
        // Block advancement messages for vanished players
        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getVanishManager().getConfig().getBoolean("block_advancements", true)) {
                event.message(null);
            }
        }
    }
    
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (plugin.getVanishManager().isVanished(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Ванишнутый игрок получает урон
        if (event.getEntity() instanceof Player victim) {
            if (plugin.getVanishManager().isVanished(victim)) {
                // От мобов - всегда отменяем
                if (!(event.getDamager() instanceof Player)) {
                    event.setCancelled(true);
                    return;
                }
                
                // От игроков - проверяем конфиг
                if (!plugin.getVanishManager().getConfig().getBoolean("allow_pvp_damage", false)) {
                    event.setCancelled(true);
                }
            }
        }
        
        // Ванишнутый игрок наносит урон
        if (event.getDamager() instanceof Player attacker) {
            if (plugin.getVanishManager().isVanished(attacker)) {
                if (event.getEntity() instanceof Player) {
                    // PvP в ванише
                    if (!plugin.getVanishManager().getConfig().getBoolean("allow_pvp_attack", false)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getVanishManager().isVanished(player)) {
                if (plugin.getVanishManager().getConfig().getBoolean("block_pickup", false)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getVanishManager().isVanished(player)) {
            return;
        }
        
        // Блокируем открытие контейнеров с анимацией/звуком
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            
            // Проверяем, является ли блок контейнером
            if (block.getState() instanceof Container) {
                if (plugin.getVanishManager().getConfig().getBoolean("silent_containers", true)) {
                    // Открываем инвентарь напрямую без анимации
                    event.setCancelled(true);
                    Container container = (Container) block.getState();
                    player.openInventory(container.getInventory());
                }
            }
            
            // Блокируем взаимодействие с дверями/люками/калитками
            if (plugin.getVanishManager().getConfig().getBoolean("silent_interactions", true)) {
                String blockType = block.getType().name();
                if (blockType.contains("DOOR") || blockType.contains("GATE") || 
                    blockType.contains("TRAPDOOR") || blockType.contains("FENCE_GATE")) {
                    // Не отменяем, но можно добавить логику если нужно
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getVanishManager().getConfig().getBoolean("block_drop", false)) {
                event.setCancelled(true);
            }
        }
    }
}

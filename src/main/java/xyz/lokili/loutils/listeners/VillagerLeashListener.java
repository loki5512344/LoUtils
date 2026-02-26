package xyz.lokili.loutils.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.utils.SchedulerUtil;

public class VillagerLeashListener implements Listener {
    
    private final LoUtils plugin;
    
    public VillagerLeashListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.VILLAGERLEASH)) return;
        if (event.getEntity().getType() != EntityType.VILLAGER) return;
        
        boolean leashEnabled = plugin.getConfigManager().getConfig(ConfigConstants.VILLAGERLEASH_CONFIG)
                .getBoolean("leash-enabled", true);
        
        if (leashEnabled) event.setCancelled(false);
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.VILLAGERLEASH)) return;
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.EMERALD) return;
        
        var config = plugin.getConfigManager().getConfig(ConfigConstants.VILLAGERLEASH_CONFIG);
        if (!config.getBoolean("emerald-attraction.enabled", true)) return;
        
        double speed = config.getDouble("emerald-attraction.movement-speed", 0.6);
        var player = event.getPlayer();
        
        SchedulerUtil.runForEntity(plugin, villager, () -> {
            if (villager.isValid() && player.isOnline()) {
                villager.getPathfinder().moveTo(player.getLocation(), speed);
            }
        });
    }
}

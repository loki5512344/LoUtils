package xyz.lokili.loutils.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.constants.GameplayConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.utils.SchedulerUtil;

public class VillagerLeashListener extends BaseListener {
    
    public VillagerLeashListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.VILLAGERLEASH, ConfigConstants.VILLAGERLEASH_CONFIG);
    }
    
    @EventHandler
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (!checkEnabled()) return;
        if (event.getEntity().getType() != EntityType.VILLAGER) return;
        
        boolean leashEnabled = config.getBoolean("leash-enabled", true);
        
        if (leashEnabled) event.setCancelled(false);
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!checkEnabled()) return;
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.EMERALD) return;
        
        if (!config.getBoolean("emerald-attraction.enabled", true)) return;
        
        double speed = config.getDouble("emerald-attraction.movement-speed", GameplayConstants.VILLAGER_MOVEMENT_SPEED);
        var player = event.getPlayer();
        
        SchedulerUtil.runForEntity(plugin, villager, () -> {
            if (villager.isValid() && player.isOnline()) {
                villager.getPathfinder().moveTo(player.getLocation(), speed);
            }
        });
    }
}

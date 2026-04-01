package xyz.lokili.loutils.listeners.world;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.constants.GameplayConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

public class SleepPercentageListener extends BaseListener {
    
    public SleepPercentageListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.SLEEPPERCENTAGE, ConfigConstants.SLEEPPERCENTAGE_CONFIG);
    }
    
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (!checkEnabled()) return;
        
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }
        
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        // Only work at night
        if (!isNight(world)) {
            return;
        }
        
        int sleepPercentage = moduleConfig().getInt("sleep-percentage", GameplayConstants.DEFAULT_SLEEP_PERCENTAGE);
        boolean showMessage = moduleConfig().getBoolean("show-message", true);
        
        // Count sleeping players
        long sleeping = world.getPlayers().stream()
                .filter(Player::isSleeping)
                .count();
        
        long total = world.getPlayers().size();
        int required = (int) Math.ceil(total * sleepPercentage / 100.0);
        
        if (sleeping >= required) {
            world.setTime(0);
            world.setStorm(false);
            world.setThundering(false);
            
            if (showMessage) {
                world.getPlayers().forEach(p -> 
                    p.sendMessage("§aНочь пропущена! §7(" + sleeping + "/" + total + " игроков спят)")
                );
            }
        } else if (showMessage) {
            world.getPlayers().forEach(p -> 
                p.sendMessage("§eДля пропуска ночи нужно §6" + required + "§e игроков §7(" + sleeping + "/" + required + ")")
            );
        }
    }
    
    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 12541 && time <= 23458;
    }
}

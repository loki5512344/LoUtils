package xyz.lokili.loutils.registry;

import org.bukkit.plugin.PluginManager;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.listeners.*;

/**
 * Регистрация листенеров плагина
 */
public class ListenerRegistry {
    
    private final LoUtils plugin;
    private final PluginManager pm;
    private InvSeeListener invSeeListener;
    
    public ListenerRegistry(LoUtils plugin) {
        this.plugin = plugin;
        this.pm = plugin.getServer().getPluginManager();
    }
    
    public void registerAll() {
        invSeeListener = new InvSeeListener(plugin);
        
        pm.registerEvents(new PlayerJoinListener(plugin), plugin);
        pm.registerEvents(new DeathMessageListener(plugin), plugin);
        pm.registerEvents(invSeeListener, plugin);
        pm.registerEvents(new WorldLockListener(plugin), plugin);
        pm.registerEvents(new CustomWorldHeightListener(plugin), plugin);
        pm.registerEvents(new FastLeafDecayListener(plugin), plugin);
        pm.registerEvents(new SleepPercentageListener(plugin), plugin);
        pm.registerEvents(new VillagerLeashListener(plugin), plugin);
        pm.registerEvents(new CauldronListener(plugin), plugin);
        pm.registerEvents(new LightBlockListener(plugin), plugin);
        pm.registerEvents(new InvisibleFrameListener(plugin), plugin);
        pm.registerEvents(new DebugStickListener(plugin), plugin);
    }
    
    public InvSeeListener getInvSeeListener() {
        return invSeeListener;
    }
}

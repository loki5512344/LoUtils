package xyz.lokili.loutils.registry;

import org.bukkit.plugin.PluginManager;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IConfigManager;
import xyz.lokili.loutils.listeners.*;
import xyz.lokili.loutils.services.LightParticleService;

/**
 * Регистрация листенеров плагина
 */
public class ListenerRegistry {
    
    private final LoUtils plugin;
    private final IConfigManager configManager;
    private final PluginManager pm;
    private InvSeeListener invSeeListener;
    private LightParticleService lightParticleService;
    
    public ListenerRegistry(LoUtils plugin, IConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.pm = plugin.getServer().getPluginManager();
    }
    
    public void registerAll() {
        invSeeListener = new InvSeeListener(plugin);
        lightParticleService = new LightParticleService(plugin);
        
        pm.registerEvents(new PlayerJoinListener(plugin, configManager), plugin);
        pm.registerEvents(new DeathMessageListener(plugin, configManager), plugin);
        pm.registerEvents(invSeeListener, plugin);
        pm.registerEvents(new WorldLockListener(plugin, configManager), plugin);
        pm.registerEvents(new CustomWorldHeightListener(plugin, configManager), plugin);
        pm.registerEvents(new FastLeafDecayListener(plugin, configManager), plugin);
        pm.registerEvents(new SleepPercentageListener(plugin, configManager), plugin);
        pm.registerEvents(new VillagerLeashListener(plugin, configManager), plugin);
        pm.registerEvents(new CauldronListener(plugin, configManager), plugin);
        pm.registerEvents(new LightBlockListener(plugin, configManager, lightParticleService), plugin);
        pm.registerEvents(new InvisibleFrameListener(plugin, configManager), plugin);
        pm.registerEvents(new DebugStickListener(plugin, configManager), plugin);
        pm.registerEvents(new PlayerPoseListener(plugin, configManager), plugin);
        pm.registerEvents(new xyz.lokili.loutils.listeners.crafts.CustomCraftsListener(plugin, configManager), plugin);
        pm.registerEvents(new xyz.lokili.loutils.listeners.crafts.FireworkCraftListener(plugin, configManager), plugin);
        pm.registerEvents(new MapLockListener(plugin, configManager), plugin);
        pm.registerEvents(new FrameLockListener(plugin, configManager), plugin);
        pm.registerEvents(new EnhancedBoneMealListener(plugin, configManager), plugin);
        pm.registerEvents(new AnvilRepairListener(plugin, configManager), plugin);
        pm.registerEvents(new NameTagRemovalListener(plugin, configManager), plugin);
    }
    
    public InvSeeListener getInvSeeListener() {
        return invSeeListener;
    }
}

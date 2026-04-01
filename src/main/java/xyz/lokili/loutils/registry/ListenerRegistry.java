package xyz.lokili.loutils.registry;

import org.bukkit.plugin.PluginManager;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IConfigManager;
import xyz.lokili.loutils.listeners.blocks.AnvilRepairListener;
import xyz.lokili.loutils.listeners.blocks.CauldronListener;
import xyz.lokili.loutils.listeners.blocks.LightBlockListener;
import xyz.lokili.loutils.listeners.crafts.CustomCraftsListener;
import xyz.lokili.loutils.listeners.crafts.EchoPickaxeCraftListener;
import xyz.lokili.loutils.listeners.crafts.EchoPickaxeMechanicsListener;
import xyz.lokili.loutils.listeners.crafts.ElytraCraftListener;
import xyz.lokili.loutils.listeners.crafts.ElytraMechanicsListener;
import xyz.lokili.loutils.listeners.crafts.FireworkCraftListener;
import xyz.lokili.loutils.listeners.entity.CowMilkingListener;
import xyz.lokili.loutils.listeners.entity.VillagerLeashListener;
import xyz.lokili.loutils.listeners.items.DebugStickListener;
import xyz.lokili.loutils.listeners.items.EnhancedBoneMealListener;
import xyz.lokili.loutils.listeners.items.EnhancedHoeListener;
import xyz.lokili.loutils.listeners.items.FrameLockListener;
import xyz.lokili.loutils.listeners.items.InvisibleFrameListener;
import xyz.lokili.loutils.listeners.items.InventoryCheckStickListener;
import xyz.lokili.loutils.listeners.items.MapLockListener;
import xyz.lokili.loutils.listeners.items.NameTagRemovalListener;
import xyz.lokili.loutils.listeners.player.DeathMessageListener;
import xyz.lokili.loutils.listeners.player.HandcuffsListener;
import xyz.lokili.loutils.listeners.player.InvSeeListener;
import xyz.lokili.loutils.listeners.player.PlayerJoinListener;
import xyz.lokili.loutils.listeners.player.PlayerPoseListener;
import xyz.lokili.loutils.listeners.recipes.RecipeDiscoveryListener;
import xyz.lokili.loutils.listeners.world.CustomWorldHeightListener;
import xyz.lokili.loutils.listeners.world.FastLeafDecayListener;
import xyz.lokili.loutils.listeners.world.SleepPercentageListener;
import xyz.lokili.loutils.listeners.world.WorldLockListener;
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
        pm.registerEvents(new InventoryCheckStickListener(plugin, configManager), plugin);
        pm.registerEvents(new PlayerPoseListener(plugin, configManager), plugin);
        HandcuffsListener handcuffsListener = new HandcuffsListener(plugin, configManager);
        pm.registerEvents(handcuffsListener, plugin);
        handcuffsListener.startTickTask();
        pm.registerEvents(new CustomCraftsListener(plugin, configManager), plugin);
        pm.registerEvents(new ElytraCraftListener(plugin, configManager), plugin);
        pm.registerEvents(new ElytraMechanicsListener(plugin, configManager), plugin);
        pm.registerEvents(new EchoPickaxeCraftListener(plugin, configManager), plugin);
        pm.registerEvents(new EchoPickaxeMechanicsListener(plugin, configManager), plugin);
        pm.registerEvents(new FireworkCraftListener(plugin, configManager), plugin);
        pm.registerEvents(new MapLockListener(plugin, configManager), plugin);
        pm.registerEvents(new FrameLockListener(plugin, configManager), plugin);
        pm.registerEvents(new EnhancedBoneMealListener(plugin, configManager), plugin);
        pm.registerEvents(new AnvilRepairListener(plugin, configManager), plugin);
        pm.registerEvents(new NameTagRemovalListener(plugin, configManager), plugin);
        pm.registerEvents(new EnhancedHoeListener(plugin, configManager), plugin);
        pm.registerEvents(new RecipeDiscoveryListener(plugin), plugin);

        CowMilkingListener cowMilkingListener = new CowMilkingListener(plugin, configManager);
        pm.registerEvents(cowMilkingListener, plugin);
        cowMilkingListener.startParticleTask();
    }

    public InvSeeListener getInvSeeListener() {
        return invSeeListener;
    }
}

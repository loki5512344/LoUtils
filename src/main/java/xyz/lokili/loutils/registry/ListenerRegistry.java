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
import xyz.lokili.loutils.listeners.entity.VillagerLeashListener;
import xyz.lokili.loutils.listeners.items.DebugStickListener;
import xyz.lokili.loutils.listeners.items.EnhancedBoneMealListener;
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

    private LightBlockListener lightBlockListener;
    private InvisibleFrameListener invisibleFrameListener;
    private DebugStickListener debugStickListener;
    private InventoryCheckStickListener inventoryCheckStickListener;
    private HandcuffsListener handcuffsListener;
    private CustomCraftsListener customCraftsListener;
    private ElytraCraftListener elytraCraftListener;
    private EchoPickaxeCraftListener echoPickaxeCraftListener;
    private FireworkCraftListener fireworkCraftListener;

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
        lightBlockListener = new LightBlockListener(plugin, configManager, lightParticleService);
        pm.registerEvents(lightBlockListener, plugin);
        invisibleFrameListener = new InvisibleFrameListener(plugin, configManager);
        pm.registerEvents(invisibleFrameListener, plugin);
        debugStickListener = new DebugStickListener(plugin, configManager);
        pm.registerEvents(debugStickListener, plugin);
        inventoryCheckStickListener = new InventoryCheckStickListener(plugin, configManager);
        pm.registerEvents(inventoryCheckStickListener, plugin);
        pm.registerEvents(new PlayerPoseListener(plugin, configManager), plugin);
        handcuffsListener = new HandcuffsListener(plugin, configManager);
        pm.registerEvents(handcuffsListener, plugin);
        handcuffsListener.startTickTask();
        customCraftsListener = new CustomCraftsListener(plugin, configManager);
        pm.registerEvents(customCraftsListener, plugin);
        elytraCraftListener = new ElytraCraftListener(plugin, configManager);
        pm.registerEvents(elytraCraftListener, plugin);
        pm.registerEvents(new ElytraMechanicsListener(plugin, configManager), plugin);
        echoPickaxeCraftListener = new EchoPickaxeCraftListener(plugin, configManager);
        pm.registerEvents(echoPickaxeCraftListener, plugin);
        pm.registerEvents(new EchoPickaxeMechanicsListener(plugin, configManager), plugin);
        fireworkCraftListener = new FireworkCraftListener(plugin, configManager);
        pm.registerEvents(fireworkCraftListener, plugin);
        pm.registerEvents(new MapLockListener(plugin, configManager), plugin);
        pm.registerEvents(new FrameLockListener(plugin, configManager), plugin);
        pm.registerEvents(new EnhancedBoneMealListener(plugin, configManager), plugin);
        pm.registerEvents(new AnvilRepairListener(plugin, configManager), plugin);
        pm.registerEvents(new NameTagRemovalListener(plugin, configManager), plugin);
        pm.registerEvents(new RecipeDiscoveryListener(plugin), plugin);
    }

    public InvSeeListener getInvSeeListener() {
        return invSeeListener;
    }

    /**
     * Повторная регистрация рецептов после {@code /loutils reload} (конфиги уже перечитаны).
     */
    public void reregisterRecipes() {
        if (lightBlockListener != null) {
            lightBlockListener.registerLightBlockRecipe();
        }
        if (invisibleFrameListener != null) {
            invisibleFrameListener.registerInvisibleFrameRecipe();
        }
        if (debugStickListener != null) {
            debugStickListener.registerDebugStickRecipe();
        }
        if (inventoryCheckStickListener != null) {
            inventoryCheckStickListener.registerInventoryCheckStickRecipe();
        }
        if (handcuffsListener != null) {
            handcuffsListener.registerHandcuffsRecipe();
        }
        if (customCraftsListener != null) {
            customCraftsListener.registerCrafts();
        }
        if (elytraCraftListener != null) {
            elytraCraftListener.registerElytraCraft();
        }
        if (echoPickaxeCraftListener != null) {
            echoPickaxeCraftListener.registerEchoPickaxeRecipe();
        }
        if (fireworkCraftListener != null) {
            fireworkCraftListener.registerFireworkCraft();
        }
    }
}

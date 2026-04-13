package xyz.lokili.loutils.listeners.world;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldLoadEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.managers.CustomWorldHeightManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CustomWorldHeightListener extends BaseListener {
    
    public CustomWorldHeightListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CUSTOMWORLDHEIGHT, ConfigConstants.CUSTOMWORLDHEIGHT_CONFIG);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInit(WorldLoadEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();
        
        CustomWorldHeightManager.WorldHeightConfig config = 
                plugin.getContainer().getCustomWorldHeightManager().getConfig(worldName);
        
        if (config == null) {
            return;
        }
        
        try {
            applyWorldHeight(world, config);
            plugin.getLogger().info("Applied custom height to world: " + worldName 
                    + " (height: " + config.height + ", minY: " + config.minY + ")");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to apply custom height to world " + worldName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void applyWorldHeight(World world, CustomWorldHeightManager.WorldHeightConfig config) throws Exception {
        // Получаем CraftWorld
        Class<?> craftWorldClass = world.getClass();
        Method getHandleMethod = craftWorldClass.getMethod("getHandle");
        Object serverLevel = getHandleMethod.invoke(world);
        
        // Получаем WorldDimension
        Class<?> serverLevelClass = serverLevel.getClass();
        Field dimensionField = findField(serverLevelClass, "dimension");
        if (dimensionField == null) {
            throw new Exception("Could not find dimension field");
        }
        dimensionField.setAccessible(true);
        Object dimension = dimensionField.get(serverLevel);
        
        // Модифицируем высоту через reflection
        Class<?> dimensionClass = dimension.getClass();
        
        // Устанавливаем height
        Field heightField = findField(dimensionClass, "height");
        if (heightField != null) {
            heightField.setAccessible(true);
            heightField.set(dimension, config.height);
        }
        
        // Устанавливаем minY
        Field minYField = findField(dimensionClass, "minY");
        if (minYField != null) {
            minYField.setAccessible(true);
            minYField.set(dimension, config.minY);
        }
        
        // Устанавливаем logicalHeight
        Field logicalHeightField = findField(dimensionClass, "logicalHeight");
        if (logicalHeightField != null) {
            logicalHeightField.setAccessible(true);
            logicalHeightField.set(dimension, config.logicalHeight);
        }
    }
    
    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // Пробуем найти в родительских классах
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return findField(superClass, fieldName);
            }
            return null;
        }
    }
}

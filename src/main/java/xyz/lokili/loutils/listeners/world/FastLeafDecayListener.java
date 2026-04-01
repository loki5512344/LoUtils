package xyz.lokili.loutils.listeners.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.constants.GameplayConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.utils.SchedulerUtil;

import java.util.HashSet;
import java.util.Set;

public class FastLeafDecayListener extends BaseListener {
    
    public FastLeafDecayListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.FASTLEAFDECAY, ConfigConstants.FASTLEAFDECAY_CONFIG);
    }
    
    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!checkEnabled()) {
            return;
        }
        
        Block block = event.getBlock();
        Material type = block.getType();
        
        // Проверяем, является ли блок бревном
        if (!isLog(type)) {
            return;
        }
        
        int delay = moduleConfig().getInt("decay-delay", GameplayConstants.DEFAULT_DECAY_DELAY_TICKS);
        int radius = moduleConfig().getInt("search-radius", 5);
        
        // Сохраняем локацию и тип дерева для scheduler
        final Location loc = block.getLocation().clone();
        final String logType = getWoodType(type); // Получаем тип дерева (OAK, BIRCH и т.д.)
        
        // Schedule leaf decay
        try {
            SchedulerUtil.runAtLocationDelayed(plugin, loc, () -> {
                decayLeaves(block, radius, logType);
            }, delay);
        } catch (Exception e) {
            plugin.getLogger().severe("[FastLeafDecay] Error scheduling decay: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void decayLeaves(Block center, int radius, String treeType) {
        Set<Block> leaves = new HashSet<>();
        Set<Block> logsOfSameType = new HashSet<>();
        
        // Сначала найдём все блоки ЭТОГО типа дерева в радиусе
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    Material type = block.getType();
                    
                    // Проверяем только листья и брёвна ЭТОГО типа дерева
                    if (isLeaf(type) && getWoodType(type).equals(treeType)) {
                        leaves.add(block);
                    } else if (isLog(type) && getWoodType(type).equals(treeType)) {
                        logsOfSameType.add(block);
                    }
                }
            }
        }
        
        // Ограничение на количество листьев для предотвращения лагов
        if (leaves.size() > 500) {
            plugin.loLogger().warn("FastLeafDecay: Too many leaves (" + leaves.size() + "), skipping");
            return;
        }
        
        // Удаляем листья, которые не связаны с оставшимися брёвнами ЭТОГО типа
        Set<Block> leavesToRemove = new HashSet<>();
        
        boolean smartDecay = moduleConfig().getBoolean("smart-decay", true);
        if (smartDecay && !logsOfSameType.isEmpty()) {
            // Умный алгоритм - проверяем связь с брёвнами того же типа
            for (Block leaf : leaves) {
                if (!isConnectedToLogIterative(leaf, logsOfSameType, leaves)) {
                    leavesToRemove.add(leaf);
                }
            }
        } else {
            // Простой алгоритм - удаляем ВСЕ листья этого типа (дерево срублено полностью)
            leavesToRemove.addAll(leaves);
        }
        
        // Удаляем листья постепенно с правильным дропом
        int animationDelay = moduleConfig().getInt("animation-delay", 2);
        int delay = 1;
        
        for (Block leaf : leavesToRemove) {
            final Block finalLeaf = leaf;
            final int currentDelay = delay;
            
            SchedulerUtil.runAtLocationDelayed(plugin, leaf.getLocation(), () -> {
                // Проверяем что блок всё ещё лист перед удалением
                if (isLeaf(finalLeaf.getType())) {
                    // breakNaturally() дропает предметы как при естественном гниении
                    finalLeaf.breakNaturally();
                }
            }, currentDelay);
            
            delay += Math.max(1, animationDelay);
        }
    }
    
    /**
     * Проверяет, связан ли лист с каким-либо бревном через другие листья
     * Использует итеративный алгоритм BFS для предотвращения StackOverflow
     */
    private boolean isConnectedToLogIterative(Block startLeaf, Set<Block> logs, Set<Block> allLeaves) {
        Set<Block> visited = new HashSet<>();
        java.util.Queue<Block> queue = new java.util.LinkedList<>();
        queue.add(startLeaf);
        visited.add(startLeaf);
        
        int maxIterations = 200;
        int iterations = 0;
        
        // Максимальное расстояние от листа до бревна (ванильная механика)
        final int MAX_DISTANCE_TO_LOG = 6;
        
        while (!queue.isEmpty() && iterations < maxIterations) {
            iterations++;
            Block current = queue.poll();
            
            // Проверяем соседние блоки
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        
                        Block neighbor = current.getRelative(x, y, z);
                        
                        // Если рядом есть бревно - проверяем расстояние до исходного листа
                        if (logs.contains(neighbor)) {
                            double distance = startLeaf.getLocation().distance(neighbor.getLocation());
                            if (distance <= MAX_DISTANCE_TO_LOG) {
                                return true; // Лист связан с близким бревном
                            }
                        }
                        
                        // Если рядом есть другой лист из нашего набора - добавляем в очередь
                        if (allLeaves.contains(neighbor) && !visited.contains(neighbor)) {
                            // Проверяем что мы не ушли слишком далеко от исходного листа
                            double distanceFromStart = startLeaf.getLocation().distance(neighbor.getLocation());
                            if (distanceFromStart <= MAX_DISTANCE_TO_LOG) {
                                visited.add(neighbor);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG") || material.name().endsWith("_WOOD");
    }
    
    private boolean isLeaf(Material material) {
        return material.name().endsWith("_LEAVES");
    }
    
    /**
     * Получает тип дерева из материала (OAK, BIRCH, SPRUCE и т.д.)
     * Примеры: OAK_LOG -> OAK, BIRCH_LEAVES -> BIRCH, STRIPPED_OAK_LOG -> STRIPPED_OAK
     */
    private String getWoodType(Material material) {
        String name = material.name();
        
        // Убираем суффиксы _LOG, _WOOD, _LEAVES
        if (name.endsWith("_LOG")) {
            return name.substring(0, name.length() - 4);
        } else if (name.endsWith("_WOOD")) {
            return name.substring(0, name.length() - 5);
        } else if (name.endsWith("_LEAVES")) {
            return name.substring(0, name.length() - 7);
        }
        
        return name; // Fallback
    }
}

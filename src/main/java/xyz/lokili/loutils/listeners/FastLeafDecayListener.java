package xyz.lokili.loutils.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.utils.SchedulerUtil;

import java.util.HashSet;
import java.util.Set;

public class FastLeafDecayListener implements Listener {
    
    private final LoUtils plugin;
    
    public FastLeafDecayListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.FASTLEAFDECAY)) {
            return;
        }
        
        Block block = event.getBlock();
        if (!isLog(block.getType())) {
            return;
        }
        
        int delay = plugin.getConfigManager().getConfig(ConfigConstants.FASTLEAFDECAY_CONFIG)
                .getInt("decay-delay", 40);
        int radius = plugin.getConfigManager().getConfig(ConfigConstants.FASTLEAFDECAY_CONFIG)
                .getInt("search-radius", 5);
        
        // Schedule leaf decay
        SchedulerUtil.runAtLocationDelayed(plugin, block.getLocation(), () -> decayLeaves(block, radius), delay);
    }
    
    private void decayLeaves(Block center, int radius) {
        Set<Block> leaves = new HashSet<>();
        
        // Find all leaves in radius
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    if (isLeaf(block.getType())) {
                        leaves.add(block);
                    }
                }
            }
        }
        
        // Remove leaves
        for (Block leaf : leaves) {
            leaf.setType(Material.AIR);
        }
    }
    
    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG") || material.name().endsWith("_WOOD");
    }
    
    private boolean isLeaf(Material material) {
        return material.name().endsWith("_LEAVES");
    }
}

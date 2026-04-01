package xyz.lokili.loutils.listeners.blocks;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

public class AnvilRepairListener extends BaseListener {

    public AnvilRepairListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, "anvil-repair", ConfigConstants.ANVIL_REPAIR_CONFIG);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilRepair(PlayerInteractEvent event) {
        if (!checkEnabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Material anvilType = block.getType();
        if (anvilType != Material.CHIPPED_ANVIL && anvilType != Material.DAMAGED_ANVIL) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (moduleConfig() == null) return;
        String repairItemName = moduleConfig().getString("repair-item", "IRON_BLOCK");
        if (!item.getType().name().equals(repairItemName)) return;

        // Определяем новое состояние наковальни
        Material newType = anvilType == Material.CHIPPED_ANVIL ? Material.ANVIL : Material.CHIPPED_ANVIL;
        
        // Чинимся
        block.setType(newType);
        
        // Забираем железный блок
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        // Эффекты
        if (moduleConfig().getBoolean("sounds", true)) {
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        }
        if (moduleConfig().getBoolean("particles", true)) {
            block.getWorld().spawnParticle(Particle.ITEM, 
                block.getLocation().add(0.5, 0.5, 0.5), 
                20, 0.3, 0.3, 0.3, 0.1,
                new ItemStack(Material.IRON_BLOCK));
        }

        player.sendActionBar(Colors.parse(moduleConfig().getString("messages.repaired")));
        event.setCancelled(true);
    }
}

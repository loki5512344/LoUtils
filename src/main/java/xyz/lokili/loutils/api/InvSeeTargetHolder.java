package xyz.lokili.loutils.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Holder инвентаря /linvsee: даёт доступ к игроку, чей инвентарь просматривают.
 */
public interface InvSeeTargetHolder extends InventoryHolder {

    @NotNull Player getInvSeeTarget();
}

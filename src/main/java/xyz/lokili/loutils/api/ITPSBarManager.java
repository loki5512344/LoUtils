package xyz.lokili.loutils.api;

import org.bukkit.entity.Player;

public interface ITPSBarManager {
    void toggleBar(Player player);
    void showBar(Player player);
    void hideBar(Player player);
    boolean hasBar(Player player);
    void handleQuit(Player player);
    void shutdown();
    
    // Legacy methods for backward compatibility
    default void toggleTPSBar(Player player) { toggleBar(player); }
    default void enableTPSBar(Player player) { showBar(player); }
    default void disableTPSBar(Player player) { hideBar(player); }
    default boolean hasTPSBar(Player player) { return hasBar(player); }
}

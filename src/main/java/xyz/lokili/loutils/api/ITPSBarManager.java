package xyz.lokili.loutils.api;

import org.bukkit.entity.Player;

public interface ITPSBarManager {
    void toggleBar(Player player);
    void showBar(Player player);
    void hideBar(Player player);
    boolean hasBar(Player player);
    void handleQuit(Player player);
    void shutdown();
}

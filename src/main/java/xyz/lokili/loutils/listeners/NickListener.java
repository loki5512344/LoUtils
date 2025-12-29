package xyz.lokili.loutils.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.lokili.loutils.LoUtils;

public class NickListener implements Listener {
    
    private final LoUtils plugin;
    
    public NickListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getNickManager().handleJoin(event.getPlayer());
    }
}

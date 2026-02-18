package xyz.lokili.loutils.api;

import java.util.Set;

public interface IWhitelistManager {
    boolean addPlayer(String playerName);
    boolean removePlayer(String playerName);
    boolean isWhitelisted(String playerName);
    Set<String> getWhitelistedPlayers();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    void reload();
    void saveWhitelist();
}

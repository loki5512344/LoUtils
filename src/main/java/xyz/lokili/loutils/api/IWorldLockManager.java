package xyz.lokili.loutils.api;

import java.util.Set;

public interface IWorldLockManager {
    boolean addWorld(String worldName);
    boolean removeWorld(String worldName);
    boolean isLocked(String worldName);
    Set<String> getLockedWorlds();
    void reload();
    void saveConfig();
}

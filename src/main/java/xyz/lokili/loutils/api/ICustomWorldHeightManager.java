package xyz.lokili.loutils.api;

import xyz.lokili.loutils.managers.CustomWorldHeightManager.WorldHeightConfig;

public interface ICustomWorldHeightManager {
    WorldHeightConfig getConfig(String worldName);
    void reload();
}

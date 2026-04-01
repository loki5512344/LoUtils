package xyz.lokili.loutils.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigConstantsTest {
    
    @Test
    void testConfigPaths() {
        assertNotNull(ConfigConstants.MESSAGES_CONFIG);
        assertNotNull(ConfigConstants.WHITELIST_CONFIG);
        assertNotNull(ConfigConstants.AUTORESTART_CONFIG);
        assertNotNull(ConfigConstants.POSES_CONFIG);
        
        assertTrue(ConfigConstants.WHITELIST_CONFIG.endsWith(".yml"));
        assertTrue(ConfigConstants.POSES_CONFIG.contains("conf/"));
    }
    
    @Test
    void testMessageKeys() {
        assertNotNull(ConfigConstants.Messages.NO_PERMISSION);
        assertNotNull(ConfigConstants.Messages.CONFIG_RELOADED);
        assertNotNull(ConfigConstants.Messages.PLAYER_NOT_FOUND);
        
        assertEquals("no-permission", ConfigConstants.Messages.NO_PERMISSION);
        assertEquals("config-reloaded", ConfigConstants.Messages.CONFIG_RELOADED);
    }
    
    @Test
    void testPermissions() {
        assertNotNull(ConfigConstants.Permissions.WHITELIST);
        assertNotNull(ConfigConstants.Permissions.AUTORESTART);
        assertNotNull(ConfigConstants.Permissions.ADMIN);
        
        assertTrue(ConfigConstants.Permissions.WHITELIST.startsWith("loutils."));
        assertTrue(ConfigConstants.Permissions.ADMIN.equals("loutils.admin"));
    }
    
    @Test
    void testModules() {
        assertNotNull(ConfigConstants.Modules.WHITELIST);
        assertNotNull(ConfigConstants.Modules.AUTORESTART);
        assertNotNull(ConfigConstants.Modules.POSES);
        assertNotNull(ConfigConstants.Modules.CAULDRON);
        
        assertEquals("whitelist", ConfigConstants.Modules.WHITELIST);
        assertEquals("poses", ConfigConstants.Modules.POSES);
    }
}

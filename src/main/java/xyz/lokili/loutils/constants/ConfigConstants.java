package xyz.lokili.loutils.constants;

public final class ConfigConstants {
    
    private ConfigConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // Config file paths
    public static final String MESSAGES_CONFIG = "messages.yml";
    public static final String WHITELIST_CONFIG = "conf/whitelist.yml";
    public static final String AUTORESTART_CONFIG = "conf/autorestart.yml";
    public static final String DEATH_MESSAGES_CONFIG = "conf/deathmessages.yml";
    public static final String ENCHANT_CONFIG = "conf/enchant.yml";
    public static final String TPSBAR_CONFIG = "conf/tpsbar.yml";
    public static final String WORLDLOCK_CONFIG = "conf/worldlock.yml";
    public static final String CUSTOMWORLDHEIGHT_CONFIG = "conf/customworldheight.yml";
    
    // Message keys
    public static final class Messages {
        public static final String NO_PERMISSION = "no-permission";
        public static final String CONFIG_RELOADED = "config-reloaded";
        public static final String PLAYER_NOT_FOUND = "player-not-found";
        public static final String INVALID_ARGUMENTS = "invalid-arguments";
        
        // AutoRestart
        public static final String AUTORESTART_WARNING = "autorestart.warning";
        public static final String AUTORESTART_WARNING_SECONDS = "autorestart.warning-seconds";
        public static final String AUTORESTART_NOW = "autorestart.now";
        
        private Messages() {}
    }
    
    // Permission nodes
    public static final class Permissions {
        public static final String WHITELIST = "loutils.whitelist";
        public static final String AUTORESTART = "loutils.autorestart";
        public static final String SPAWNMOB = "loutils.spawnmob";
        public static final String INVSEE = "loutils.invsee";
        public static final String ENCHANT = "loutils.enchant";
        public static final String FLY = "loutils.fly";
        public static final String FLYSPEED = "loutils.flyspeed";
        public static final String TPSBAR = "loutils.tpsbar";
        public static final String WORLDLOCK = "loutils.worldlock";
        public static final String WORLDLOCK_BYPASS = "loutils.worldlock.bypass.";
        public static final String ADMIN = "loutils.admin";
        
        private Permissions() {}
    }
    
    // Module names
    public static final class Modules {
        public static final String WHITELIST = "whitelist";
        public static final String AUTORESTART = "autorestart";
        public static final String DEATH_MESSAGES = "deathmessages";
        public static final String ENCHANT = "enchant";
        public static final String TPSBAR = "tpsbar";
        public static final String INVSEE = "invsee";
        public static final String SPAWNMOB = "spawnmob";
        public static final String FLY = "fly";
        public static final String WORLDLOCK = "worldlock";
        public static final String CUSTOMWORLDHEIGHT = "customworldheight";
        
        private Modules() {}
    }
}

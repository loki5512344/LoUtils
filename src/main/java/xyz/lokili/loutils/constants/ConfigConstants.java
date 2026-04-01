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
    public static final String FASTLEAFDECAY_CONFIG = "conf/fastleafdecay.yml";
    public static final String SLEEPPERCENTAGE_CONFIG = "conf/sleeppercentage.yml";
    public static final String VILLAGERLEASH_CONFIG = "conf/villagerleash.yml";
    public static final String CAULDRON_CONFIG = "conf/cauldron.yml";
    public static final String LIGHT_BLOCK_CONFIG = "conf/light-block.yml";
    public static final String DEBUG_STICK_CONFIG = "conf/debug-stick.yml";
    public static final String INVISIBLE_FRAMES_CONFIG = "conf/invisible-frames.yml";
    public static final String POSES_CONFIG = "conf/poses.yml";
    public static final String CUSTOM_CRAFTS_CONFIG = "conf/custom-crafts.yml";
    public static final String MAP_LOCKING_CONFIG = "conf/map-locking.yml";
    public static final String FRAME_LOCKING_CONFIG = "conf/frame-locking.yml";
    public static final String ENHANCED_BONE_MEAL_CONFIG = "conf/enhanced-bone-meal.yml";
    public static final String ANVIL_REPAIR_CONFIG = "conf/anvil-repair.yml";
    public static final String NAME_TAG_REMOVAL_CONFIG = "conf/name-tag-removal.yml";
    public static final String ENHANCED_HOES_CONFIG = "conf/enhanced-hoes.yml";
    public static final String COW_MILKING_CONFIG = "conf/cow-milking.yml";
    public static final String INVENTORY_CHECK_STICK_CONFIG = "conf/inventory-check-stick.yml";
    public static final String HANDCUFFS_CONFIG = "conf/handcuffs.yml";
    
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
        public static final String DEBUG_STICK = "loutils.debugstick";
        public static final String INVENTORY_CHECK_STICK = "loutils.inventorycheckstick";
        public static final String HANDCUFFS = "loutils.handcuffs";
        public static final String HANDCUFFS_BYPASS = "loutils.handcuffs.bypass";
        
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
        public static final String FASTLEAFDECAY = "fastleafdecay";
        public static final String SLEEPPERCENTAGE = "sleeppercentage";
        public static final String VILLAGERLEASH = "villagerleash";
        public static final String CAULDRON = "cauldron";
        public static final String LIGHT_BLOCK = "light-block";
        public static final String DEBUG_STICK = "debug-stick";
        public static final String INVISIBLE_FRAMES = "invisible-frames";
        public static final String POSES = "poses";
        public static final String CUSTOM_CRAFTS = "custom-crafts";
        public static final String MAP_LOCKING = "map-locking";
        public static final String FRAME_LOCKING = "frame-locking";
        public static final String ENHANCED_BONE_MEAL = "enhanced-bone-meal";
        public static final String ANVIL_REPAIR = "anvil-repair";
        public static final String NAME_TAG_REMOVAL = "name-tag-removal";
        public static final String ENHANCED_HOES = "enhanced-hoes";
        public static final String COW_MILKING = "cow-milking";
        public static final String INVENTORY_CHECK_STICK = "inventory-check-stick";
        public static final String HANDCUFFS = "handcuffs";
        
        private Modules() {}
    }
}

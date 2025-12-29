package xyz.lokili.loutils.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class NickManager {
    
    private final LoUtils plugin;
    private final Map<UUID, String> nicknames;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public NickManager(LoUtils plugin) {
        this.plugin = plugin;
        this.nicknames = new HashMap<>();
        loadData();
    }
    
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data/nicknames.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create nicknames.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        if (dataConfig.contains("nicknames")) {
            for (String uuidStr : dataConfig.getConfigurationSection("nicknames").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String nick = dataConfig.getString("nicknames." + uuidStr);
                    nicknames.put(uuid, nick);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
    
    public void saveData() {
        for (Map.Entry<UUID, String> entry : nicknames.entrySet()) {
            dataConfig.set("nicknames." + entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save nicknames.yml: " + e.getMessage());
        }
    }
    
    public NickResult setNick(Player player, String nick) {
        FileConfiguration config = plugin.getConfigManager().getConfig("conf/nick.yml");
        
        // Убираем цветовые коды для проверки длины
        String strippedNick = stripColors(nick);
        
        int maxLength = config.getInt("max_length", 24);
        int minLength = config.getInt("min_length", 3);
        
        if (strippedNick.length() > maxLength) {
            return NickResult.TOO_LONG;
        }
        
        if (strippedNick.length() < minLength) {
            return NickResult.TOO_SHORT;
        }
        
        // Проверка разрешённых символов
        String pattern = config.getString("allowed_pattern", "^[a-zA-Zа-яА-ЯёЁ0-9_ ]+$");
        if (!Pattern.matches(pattern, strippedNick)) {
            return NickResult.INVALID_CHARS;
        }
        
        // Проверка чёрного списка
        List<String> blacklist = config.getStringList("blacklist");
        String lowerNick = strippedNick.toLowerCase();
        for (String banned : blacklist) {
            if (lowerNick.contains(banned.toLowerCase())) {
                return NickResult.BLACKLISTED;
            }
        }
        
        // Обработка цветов если разрешено
        String finalNick = nick;
        if (!config.getBoolean("allow_colors", true)) {
            finalNick = strippedNick;
        } else if (!config.getBoolean("allow_formatting", true)) {
            // Убираем только форматирование, оставляем цвета
            finalNick = nick.replaceAll("&[klmnoKLMNO]", "");
        }
        
        nicknames.put(player.getUniqueId(), finalNick);
        saveData();
        
        // Обновляем отображаемое имя
        updateDisplayName(player);
        
        return NickResult.SUCCESS;
    }
    
    public void resetNick(Player player) {
        nicknames.remove(player.getUniqueId());
        player.displayName(ColorUtil.colorize(player.getName()));
        saveData();
    }
    
    public String getNick(Player player) {
        return nicknames.get(player.getUniqueId());
    }
    
    public String getNick(UUID uuid) {
        return nicknames.get(uuid);
    }
    
    public boolean hasNick(Player player) {
        return nicknames.containsKey(player.getUniqueId());
    }
    
    public void updateDisplayName(Player player) {
        String nick = nicknames.get(player.getUniqueId());
        if (nick != null) {
            player.displayName(ColorUtil.colorize(nick));
        }
    }
    
    public void handleJoin(Player player) {
        if (hasNick(player)) {
            updateDisplayName(player);
        }
    }
    
    private String stripColors(String text) {
        // Убираем hex цвета &#RRGGBB
        text = text.replaceAll("&#[A-Fa-f0-9]{6}", "");
        // Убираем legacy коды &X
        text = text.replaceAll("&[0-9a-fA-FklmnorKLMNOR]", "");
        return text;
    }
    
    public String getDisplayName(Player player) {
        String nick = nicknames.get(player.getUniqueId());
        return nick != null ? nick : player.getName();
    }
    
    public enum NickResult {
        SUCCESS,
        TOO_LONG,
        TOO_SHORT,
        INVALID_CHARS,
        BLACKLISTED
    }
}

package xyz.lokili.loutils.managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PerformanceProfiler {
    
    private final LoUtils plugin;
    private ScheduledTask monitorTask;
    private long lastReportTime = 0;
    
    public PerformanceProfiler(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    public void start() {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.PERFORMANCE_CONFIG);
        if (!config.getBoolean("enabled", true)) return;
        
        int interval = config.getInt("check-interval", 30);
        
        monitorTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {
            checkPerformance();
        }, interval, interval, TimeUnit.SECONDS);
        
        plugin.getLogger().info("Performance Profiler started (check interval: " + interval + "s)");
    }
    
    public void stop() {
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
    }
    
    private void checkPerformance() {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.PERFORMANCE_CONFIG);
        
        double tpsThreshold = config.getDouble("tps-threshold", 15.0);
        double currentTPS = Bukkit.getTPS()[0];
        
        if (currentTPS >= tpsThreshold) return;
        
        // Check cooldown
        long cooldown = config.getLong("report-cooldown", 300) * 1000L;
        long now = System.currentTimeMillis();
        if (now - lastReportTime < cooldown) return;
        
        lastReportTime = now;
        
        // Generate report
        generateAndSendReport(currentTPS);
    }
    
    private void generateAndSendReport(double currentTPS) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.PERFORMANCE_CONFIG);
        
        StringBuilder report = new StringBuilder();
        report.append("**🔴 Performance Alert**\n");
        report.append("```\n");
        report.append(String.format("TPS: %.2f (threshold: %.1f)\n", currentTPS, config.getDouble("tps-threshold", 15.0)));
        report.append(String.format("MSPT: %.2f ms\n", Bukkit.getAverageTickTime()));
        report.append("```\n\n");
        
        // Analyze all worlds
        for (World world : Bukkit.getWorlds()) {
            analyzeWorld(world, report, config);
        }
        
        // Send to webhook
        sendToWebhook(report.toString(), config);
    }
    
    private void analyzeWorld(World world, StringBuilder report, org.bukkit.configuration.file.FileConfiguration config) {
        report.append("**World: ").append(world.getName()).append("**\n");
        
        // Count entities by type
        Map<EntityType, Integer> entityCounts = new HashMap<>();
        List<Player> players = new ArrayList<>();
        
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player player) {
                players.add(player);
            } else {
                entityCounts.merge(entity.getType(), 1, Integer::sum);
            }
        }
        
        // Top entities
        report.append("```\n");
        report.append("Entities:\n");
        entityCounts.entrySet().stream()
                .sorted(Map.Entry.<EntityType, Integer>comparingByValue().reversed())
                .limit(config.getInt("max-entities-in-report", 20))
                .forEach(e -> report.append(String.format("  %s: %d\n", e.getKey().name(), e.getValue())));
        
        report.append("\nPlayers: ").append(players.size()).append("\n");
        players.stream()
                .limit(config.getInt("max-players-in-report", 10))
                .forEach(p -> report.append(String.format("  %s (%.1f, %.1f, %.1f)\n", 
                        p.getName(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ())));
        report.append("```\n");
        
        // Detailed chunk analysis
        if (config.getBoolean("detailed-chunk-analysis", true)) {
            analyzeChunks(world, report, config);
        }
        
        report.append("\n");
    }
    
    private void analyzeChunks(World world, StringBuilder report, org.bukkit.configuration.file.FileConfiguration config) {
        int threshold = config.getInt("entity-per-chunk-threshold", 50);
        
        Map<String, Integer> laggyChunks = new HashMap<>();
        
        for (Chunk chunk : world.getLoadedChunks()) {
            int entityCount = chunk.getEntities().length;
            if (entityCount >= threshold) {
                String key = String.format("(%d, %d)", chunk.getX(), chunk.getZ());
                laggyChunks.put(key, entityCount);
            }
        }
        
        if (!laggyChunks.isEmpty()) {
            report.append("**⚠️ Laggy Chunks (>").append(threshold).append(" entities):**\n");
            report.append("```\n");
            laggyChunks.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .forEach(e -> report.append(String.format("  Chunk %s: %d entities\n", e.getKey(), e.getValue())));
            report.append("```\n");
        }
    }
    
    private void sendToWebhook(String content, org.bukkit.configuration.file.FileConfiguration config) {
        String webhookUrl = config.getString("webhook-url", "");
        if (webhookUrl.isEmpty()) {
            plugin.getLogger().warning("Performance alert triggered but webhook-url is not configured!");
            return;
        }
        
        Bukkit.getAsyncScheduler().runNow(plugin, (task) -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                String json = String.format("{\"content\":\"%s\"}", 
                        content.replace("\"", "\\\"").replace("\n", "\\n"));
                
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 204 || responseCode == 200) {
                    plugin.getLogger().info("Performance report sent to webhook");
                } else {
                    plugin.getLogger().warning("Failed to send webhook: HTTP " + responseCode);
                }
                
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send webhook: " + e.getMessage());
            }
        });
    }
}

package xyz.lokili.loutils.managers.performance;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Отправка отчетов в Discord webhook
 * Single Responsibility: Webhook communication
 */
public class WebhookSender {
    
    private final Plugin plugin;
    
    public WebhookSender(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Отправляет контент в webhook асинхронно
     */
    public void sendAsync(String content, String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            plugin.getLogger().warning("Performance alert triggered but webhook-url is not configured!");
            return;
        }
        
        Bukkit.getAsyncScheduler().runNow(plugin, (task) -> {
            send(content, webhookUrl);
        });
    }
    
    /**
     * Синхронная отправка в webhook
     */
    private void send(String content, String webhookUrl) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String json = buildJson(content);
            
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
    }
    
    /**
     * Создает JSON для Discord webhook
     */
    private String buildJson(String content) {
        // Экранирование специальных символов
        String escaped = content
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
        
        return String.format("{\"content\":\"%s\"}", escaped);
    }
}

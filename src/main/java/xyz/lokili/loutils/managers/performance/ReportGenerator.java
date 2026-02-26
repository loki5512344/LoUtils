package xyz.lokili.loutils.managers.performance;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Генерация отчетов о производительности
 * Single Responsibility: Report generation
 */
public class ReportGenerator {
    
    private final WorldAnalyzer worldAnalyzer;
    
    public ReportGenerator() {
        this.worldAnalyzer = new WorldAnalyzer();
    }
    
    /**
     * Генерирует полный отчет о производительности
     */
    public String generateReport(PerformanceMonitor.PerformanceMetrics metrics, FileConfiguration config) {
        StringBuilder report = new StringBuilder();
        
        // Заголовок
        appendHeader(report, metrics);
        
        // Анализ всех миров
        for (World world : Bukkit.getWorlds()) {
            appendWorldAnalysis(report, world, config);
        }
        
        return report.toString();
    }
    
    /**
     * Добавляет заголовок отчета
     */
    private void appendHeader(StringBuilder report, PerformanceMonitor.PerformanceMetrics metrics) {
        report.append("**🔴 Performance Alert**\n");
        report.append("```\n");
        report.append(String.format("TPS: %.2f (threshold: %.1f)\n", 
            metrics.getTps(), metrics.getThreshold()));
        report.append(String.format("MSPT: %.2f ms\n", metrics.getMspt()));
        report.append("```\n\n");
    }
    
    /**
     * Добавляет анализ мира
     */
    private void appendWorldAnalysis(StringBuilder report, World world, FileConfiguration config) {
        int maxEntities = config.getInt("max-entities-in-report", 20);
        int maxPlayers = config.getInt("max-players-in-report", 10);
        
        var analysis = worldAnalyzer.analyzeWorld(world, maxEntities, maxPlayers);
        
        report.append("**World: ").append(analysis.worldName()).append("**\n");
        report.append("```\n");
        report.append("Entities:\n");
        
        for (var entity : analysis.topEntities()) {
            report.append(String.format("  %s: %d\n", entity.type().name(), entity.count()));
        }
        
        report.append("\nPlayers: ").append(analysis.totalPlayers()).append("\n");
        for (var player : analysis.players()) {
            report.append(String.format("  %s (%.1f, %.1f, %.1f)\n",
                player.name(), player.x(), player.y(), player.z()));
        }
        
        report.append("```\n");
        
        // Детальный анализ чанков
        if (config.getBoolean("detailed-chunk-analysis", true)) {
            appendChunkAnalysis(report, world, config);
        }
        
        report.append("\n");
    }
    
    /**
     * Добавляет анализ чанков
     */
    private void appendChunkAnalysis(StringBuilder report, World world, FileConfiguration config) {
        int threshold = config.getInt("entity-per-chunk-threshold", 50);
        var chunkAnalysis = worldAnalyzer.analyzeChunks(world, threshold);
        
        if (!chunkAnalysis.laggyChunks().isEmpty()) {
            report.append("**⚠️ Laggy Chunks (>").append(threshold).append(" entities):**\n");
            report.append("```\n");
            
            for (var chunk : chunkAnalysis.laggyChunks()) {
                report.append(String.format("  Chunk %s: %d entities\n",
                    chunk.location(), chunk.entityCount()));
            }
            
            report.append("```\n");
        }
    }
}

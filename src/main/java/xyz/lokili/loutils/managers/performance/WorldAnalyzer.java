package xyz.lokili.loutils.managers.performance;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Анализ миров и чанков
 * Single Responsibility: World/chunk analysis
 */
public class WorldAnalyzer {
    
    /**
     * Анализирует мир и возвращает результат
     */
    public WorldAnalysisResult analyzeWorld(World world, int maxEntities, int maxPlayers) {
        Map<EntityType, Integer> entityCounts = new HashMap<>();
        List<PlayerInfo> players = new ArrayList<>();
        
        // Подсчет энтити
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Player player) {
                players.add(new PlayerInfo(
                    player.getName(),
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ()
                ));
            } else {
                entityCounts.merge(entity.getType(), 1, Integer::sum);
            }
        }
        
        // Сортировка энтити по количеству
        List<EntityCount> topEntities = entityCounts.entrySet().stream()
            .map(e -> new EntityCount(e.getKey(), e.getValue()))
            .sorted(Comparator.comparingInt(EntityCount::count).reversed())
            .limit(maxEntities)
            .toList();
        
        // Ограничение списка игроков
        List<PlayerInfo> limitedPlayers = players.stream()
            .limit(maxPlayers)
            .toList();
        
        return new WorldAnalysisResult(world.getName(), topEntities, limitedPlayers, players.size());
    }
    
    /**
     * Анализирует чанки мира на предмет лагов
     */
    public ChunkAnalysisResult analyzeChunks(World world, int entityThreshold) {
        Map<ChunkLocation, Integer> laggyChunks = new HashMap<>();
        
        for (Chunk chunk : world.getLoadedChunks()) {
            int entityCount = chunk.getEntities().length;
            if (entityCount >= entityThreshold) {
                laggyChunks.put(
                    new ChunkLocation(chunk.getX(), chunk.getZ()),
                    entityCount
                );
            }
        }
        
        // Сортировка по количеству энтити
        List<ChunkInfo> sortedChunks = laggyChunks.entrySet().stream()
            .map(e -> new ChunkInfo(e.getKey(), e.getValue()))
            .sorted(Comparator.comparingInt(ChunkInfo::entityCount).reversed())
            .limit(10)
            .toList();
        
        return new ChunkAnalysisResult(sortedChunks, entityThreshold);
    }
    
    // === Data classes ===
    
    public record WorldAnalysisResult(
        String worldName,
        List<EntityCount> topEntities,
        List<PlayerInfo> players,
        int totalPlayers
    ) {}
    
    public record EntityCount(EntityType type, int count) {}
    
    public record PlayerInfo(String name, double x, double y, double z) {}
    
    public record ChunkAnalysisResult(List<ChunkInfo> laggyChunks, int threshold) {}
    
    public record ChunkInfo(ChunkLocation location, int entityCount) {}
    
    public record ChunkLocation(int x, int z) {
        @Override
        public String toString() {
            return String.format("(%d, %d)", x, z);
        }
    }
}

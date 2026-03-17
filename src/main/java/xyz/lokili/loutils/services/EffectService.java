package xyz.lokili.loutils.services;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import xyz.lokili.loutils.constants.GameplayConstants;

/**
 * Сервис для воспроизведения эффектов (звуки, партиклы)
 * Устраняет дублирование кода эффектов
 * Применяет Service Pattern
 */
public class EffectService {
    
    /**
     * Воспроизводит эффект на блоке
     */
    public void playBlockEffect(Block block, Sound sound, Particle particle, int particleCount) {
        Location loc = block.getLocation().add(
            GameplayConstants.BLOCK_CENTER_OFFSET,
            GameplayConstants.BLOCK_CENTER_OFFSET,
            GameplayConstants.BLOCK_CENTER_OFFSET
        );
        World world = block.getWorld();
        
        playEffect(world, loc, sound, particle, particleCount);
    }
    
    /**
     * Воспроизводит эффект на сущности
     */
    public void playEntityEffect(Entity entity, Sound sound, Particle particle, int particleCount) {
        Location loc = entity.getLocation();
        World world = entity.getWorld();
        
        playEffect(world, loc, sound, particle, particleCount);
    }
    
    /**
     * Воспроизводит эффект в локации
     */
    public void playLocationEffect(Location location, Sound sound, Particle particle, int particleCount) {
        playEffect(location.getWorld(), location, sound, particle, particleCount);
    }
    
    /**
     * Базовый метод воспроизведения эффекта
     */
    private void playEffect(World world, Location location, Sound sound, Particle particle, int particleCount) {
        // Звук
        if (sound != null) {
            world.playSound(location, sound, 1.0f, 1.0f);
        }
        
        // Партиклы
        if (particle != null) {
            world.spawnParticle(
                particle, 
                location, 
                particleCount,
                GameplayConstants.PARTICLE_SPREAD_X,
                GameplayConstants.PARTICLE_SPREAD_Y,
                GameplayConstants.PARTICLE_SPREAD_Z
            );
        }
    }
    
    /**
     * Воспроизводит только звук
     */
    public void playSound(Location location, Sound sound) {
        playEffect(location.getWorld(), location, sound, null, 0);
    }
    
    /**
     * Воспроизводит только партиклы
     */
    public void playParticles(Location location, Particle particle, int count) {
        playEffect(location.getWorld(), location, null, particle, count);
    }
    
    /**
     * Воспроизводит звук с кастомной громкостью и высотой
     */
    public void playSoundCustom(Location location, Sound sound, float volume, float pitch) {
        location.getWorld().playSound(location, sound, volume, pitch);
    }
    
    /**
     * Воспроизводит партиклы с кастомными параметрами
     */
    public void playParticlesCustom(Location location, Particle particle, int count, 
                                    double offsetX, double offsetY, double offsetZ, double speed) {
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }
}

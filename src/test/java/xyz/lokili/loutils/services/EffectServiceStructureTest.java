package xyz.lokili.loutils.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Простые тесты для EffectService
 */
class EffectServiceStructureTest {
    
    @Test
    void testServiceClassExists() {
        assertNotNull(EffectService.class);
    }
    
    @Test
    void testServiceCanBeInstantiated() {
        assertDoesNotThrow(() -> new EffectService());
    }
    
    @Test
    void testServiceIsPublic() {
        assertTrue(java.lang.reflect.Modifier.isPublic(EffectService.class.getModifiers()));
    }
}

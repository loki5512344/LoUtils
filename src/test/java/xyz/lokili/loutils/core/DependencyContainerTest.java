package xyz.lokili.loutils.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Простые тесты для DependencyContainer
 */
class DependencyContainerTest {
    
    @Test
    void testContainerClassExists() {
        assertNotNull(DependencyContainer.class);
    }
    
    @Test
    void testContainerIsPublic() {
        assertTrue(java.lang.reflect.Modifier.isPublic(DependencyContainer.class.getModifiers()));
    }
    
    @Test
    void testContainerIsNotAbstract() {
        assertFalse(java.lang.reflect.Modifier.isAbstract(DependencyContainer.class.getModifiers()));
    }
}

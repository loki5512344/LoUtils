package xyz.lokili.loutils.factories;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Простые тесты для ItemFactory
 */
class ItemFactoryStructureTest {
    
    @Test
    void testFactoryClassExists() {
        assertNotNull(ItemFactory.class);
    }
    
    @Test
    void testFactoryIsPublic() {
        assertTrue(java.lang.reflect.Modifier.isPublic(ItemFactory.class.getModifiers()));
    }
}

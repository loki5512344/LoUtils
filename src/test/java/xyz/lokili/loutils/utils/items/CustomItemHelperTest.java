package xyz.lokili.loutils.utils.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Тесты для CustomItemHelper
 */
class CustomItemHelperTest {

    @Mock
    private Plugin plugin;

    @Mock
    private ItemStack itemStack;

    @Mock
    private ItemMeta itemMeta;

    @Mock
    private PersistentDataContainer pdc;

    private TestCustomItemHelper helper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plugin.getName()).thenReturn("TestPlugin");
        helper = new TestCustomItemHelper(plugin);
    }

    @Test
    void testIsCustomItem_NullStack() {
        assertFalse(helper.isCustomItem(null));
    }

    @Test
    void testIsCustomItem_WrongType() {
        when(itemStack.getType()).thenReturn(Material.STONE);

        assertFalse(helper.isCustomItem(itemStack));
    }

    @Test
    void testIsCustomItem_NullMeta() {
        when(itemStack.getType()).thenReturn(Material.DIAMOND_SWORD);
        when(itemStack.getItemMeta()).thenReturn(null);

        assertFalse(helper.isCustomItem(itemStack));
    }

    @Test
    void testMarkAsCustom_NullStack() {
        assertDoesNotThrow(() -> helper.markAsCustom(null));
    }

    @Test
    void testMarkAsCustom_WrongType() {
        when(itemStack.getType()).thenReturn(Material.STONE);

        helper.markAsCustom(itemStack);

        verify(itemStack, never()).setItemMeta(any());
    }

    /**
     * Тестовая реализация CustomItemHelper
     */
    private static class TestCustomItemHelper extends CustomItemHelper {
        protected TestCustomItemHelper(Plugin plugin) {
            super(plugin, "test_marker", Material.DIAMOND_SWORD);
        }
    }
}

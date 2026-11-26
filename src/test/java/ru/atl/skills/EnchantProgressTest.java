package ru.atl.skills;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnchantProgressTest {
    @Test void upgradesAtThresholdAndResetsProgress() {
        assertEquals(new EnchantProgress.Advance(1, 99), EnchantProgress.advance(1, 5, 98));
        assertEquals(new EnchantProgress.Advance(2, 0), EnchantProgress.advance(1, 5, 99));
        assertEquals(new EnchantProgress.Advance(2, 199), EnchantProgress.advance(2, 5, 198));
        assertEquals(new EnchantProgress.Advance(3, 0), EnchantProgress.advance(2, 5, 199));
    }
    @Test void respectsMaximumAndRejectsInvalidProgress() {
        assertEquals(new EnchantProgress.Advance(5, 0), EnchantProgress.advance(5, 5, 500));
        assertEquals(new EnchantProgress.Advance(6, 0), EnchantProgress.advance(6, 5, 500));
        assertEquals(new EnchantProgress.Advance(1, 1), EnchantProgress.advance(1, 5, -100));
        assertEquals(10000, EnchantProgress.cost(Integer.MAX_VALUE));
    }
}

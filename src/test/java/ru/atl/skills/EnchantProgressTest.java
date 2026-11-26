package ru.atl.skills;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnchantProgressTest {
    @Test void upgradesAtThresholdAndResetsProgress() {
        assertEquals(new EnchantMath.Advance(1, 99), EnchantMath.advance(1, 5, 98));
        assertEquals(new EnchantMath.Advance(2, 0), EnchantMath.advance(1, 5, 99));
        assertEquals(new EnchantMath.Advance(2, 199), EnchantMath.advance(2, 5, 198));
        assertEquals(new EnchantMath.Advance(3, 0), EnchantMath.advance(2, 5, 199));
    }
    @Test void armorProgressUsesLowerThresholdsAndPreservesExcess() {
        assertEquals(20, EnchantMath.cost(1, true));
        assertEquals(40, EnchantMath.cost(2, true));
        assertEquals(100, EnchantMath.cost(1, false));
        assertEquals(new EnchantMath.Advance(1, 19), EnchantMath.advance(1, 4, 18, true));
        assertEquals(new EnchantMath.Advance(2, 0), EnchantMath.advance(1, 4, 19, true));
        assertEquals(new EnchantMath.Advance(2, 61), EnchantMath.advance(1, 4, 80, true));
        assertEquals(new EnchantMath.Advance(3, 22), EnchantMath.advance(2, 4, 61, true));
    }
    @Test void respectsMaximumAndRejectsInvalidProgress() {
        assertEquals(new EnchantMath.Advance(5, 0), EnchantMath.advance(5, 5, 500));
        assertEquals(new EnchantMath.Advance(6, 0), EnchantMath.advance(6, 5, 500));
        assertEquals(new EnchantMath.Advance(1, 1), EnchantMath.advance(1, 5, -100));
        assertEquals(10000, EnchantMath.cost(Integer.MAX_VALUE));
    }
}

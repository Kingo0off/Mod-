package ru.atl.skills;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnchantProgressTest {
    @Test void upgradesAtThresholdAndResetsProgress() {
        assertEquals(new EnchantMath.Advance(1, 299), EnchantMath.advance(1, 5, 298));
        assertEquals(new EnchantMath.Advance(2, 0), EnchantMath.advance(1, 5, 299));
        assertEquals(new EnchantMath.Advance(2, 599), EnchantMath.advance(2, 5, 598));
        assertEquals(new EnchantMath.Advance(3, 0), EnchantMath.advance(2, 5, 599));
    }
    @Test void armorProgressUsesLowerThresholdsAndPreservesExcess() {
        assertEquals(20, EnchantMath.cost(1, true));
        assertEquals(40, EnchantMath.cost(2, true));
        assertEquals(300, EnchantMath.cost(1, false));
        assertEquals(new EnchantMath.Advance(1, 19), EnchantMath.advance(1, 4, 18, true));
        assertEquals(new EnchantMath.Advance(2, 0), EnchantMath.advance(1, 4, 19, true));
        assertEquals(new EnchantMath.Advance(2, 61), EnchantMath.advance(1, 4, 80, true));
        assertEquals(new EnchantMath.Advance(3, 22), EnchantMath.advance(2, 4, 61, true));
    }
    @Test void respectsMaximumAndRejectsInvalidProgress() {
        assertEquals(new EnchantMath.Advance(5, 0), EnchantMath.advance(5, 5, 500));
        assertEquals(new EnchantMath.Advance(6, 0), EnchantMath.advance(6, 5, 500));
        assertEquals(new EnchantMath.Advance(1, 1), EnchantMath.advance(1, 5, -100));
        assertEquals(30000, EnchantMath.cost(Integer.MAX_VALUE));
    }
}

package ru.atl.skills;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnchantBalanceTest {
    @Test void rarerEnchantmentsRequireMore() {
        assertEquals(0, EnchantMath.shelves(10));
        assertEquals(5, EnchantMath.shelves(5));
        assertEquals(10, EnchantMath.shelves(2));
        assertEquals(15, EnchantMath.shelves(1));
        assertTrue(EnchantMath.levels(1) > EnchantMath.levels(10));
        assertEquals(20, EnchantMath.cost(1, true, 10));
        assertEquals(80, EnchantMath.cost(1, true, 1));
        assertEquals(300, EnchantMath.cost(1, false, 10));
        assertEquals(1200, EnchantMath.cost(1, false, 1));
    }
    @Test void topEnchantmentsAndSharpnessHaveExplicitRequirements() {
        assertEquals(25, EnchantMath.shelves("minecraft:fortune", 2, false));
        assertEquals(25, EnchantMath.shelves("minecraft:looting", 2, false));
        assertTrue(EnchantMath.shelves("minecraft:sharpness", 10, false) > 0);
        assertTrue(EnchantMath.levels("minecraft:sharpness", 10, false) > 1);
        assertEquals(EnchantMath.shelves(2), EnchantMath.shelves("minecraft:mending", 2, true));
        assertEquals(EnchantMath.levels(2), EnchantMath.levels("minecraft:mending", 2, true));
    }
    @Test void progressBarTracksRealExperienceAndClamps() {
        assertEquals(0, EnchantMath.barFill(0, 300, 20));
        assertEquals(10, EnchantMath.barFill(150, 300, 20));
        assertEquals(20, EnchantMath.barFill(300, 300, 20));
        assertEquals(20, EnchantMath.barFill(500, 300, 20));
        assertEquals(0, EnchantMath.barFill(-2, 300, 20));
    }
    @Test void existingProgressCarriesOverAndDoesNotReset() {
        assertEquals(new EnchantMath.Advance(1, 79), EnchantMath.advance(1, 5, 78, true, 1));
        assertEquals(new EnchantMath.Advance(2, 0), EnchantMath.advance(1, 5, 79, true, 1));
        assertEquals(new EnchantMath.Advance(2, 61), EnchantMath.advance(1, 5, 80, true, 10));
    }
}

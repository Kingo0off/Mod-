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
        assertEquals(100, EnchantMath.cost(1, false, 10));
        assertEquals(400, EnchantMath.cost(1, false, 1));
    }
    @Test void existingProgressCarriesOverAndDoesNotReset() {
        assertEquals(new EnchantMath.Advance(1, 79), EnchantMath.advance(1, 5, 78, true, 1));
        assertEquals(new EnchantMath.Advance(2, 0), EnchantMath.advance(1, 5, 79, true, 1));
        assertEquals(new EnchantMath.Advance(2, 61), EnchantMath.advance(1, 5, 80, true, 10));
    }
}

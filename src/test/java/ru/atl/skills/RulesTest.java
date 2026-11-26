package ru.atl.skills;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RulesTest {
    @Test void tiersAndTypes() {
        assertEquals(new Rules.Requirement(Rules.Skill.PICKAXE, 50), Rules.requirement("minecraft:netherite_pickaxe"));
        assertEquals(new Rules.Requirement(Rules.Skill.AXE, 15), Rules.requirement("minecraft:iron_axe"));
        assertEquals(new Rules.Requirement(Rules.Skill.SWORD, 30), Rules.requirement("minecraft:diamond_sword"));
        assertNull(Rules.requirement("minecraft:diamond")); assertNull(Rules.requirement("other:diamond_pickaxe"));
    }
    @Test void mobRewardsAndIncreasingLevelCosts() {
        assertEquals(20, Rules.mobXp(20));
        assertEquals(40, Rules.mobXp(40));
        assertEquals(0, Rules.mobXp(Float.NaN));
        assertEquals(Rules.threshold(50), Rules.mobXp(Float.MAX_VALUE));
        for (int level = 1; level < 49; level++) {
            assertTrue(Rules.threshold(level + 2) - Rules.threshold(level + 1) > Rules.threshold(level + 1) - Rules.threshold(level));
        }
    }
    @Test void levelBoundaries() {
        assertEquals(1, Rules.level(0)); assertEquals(1, Rules.level(19)); assertEquals(2, Rules.level(20));
        assertEquals(49, Rules.level(Rules.threshold(50) - 1)); assertEquals(50, Rules.level(Rules.threshold(50)));
    }
}

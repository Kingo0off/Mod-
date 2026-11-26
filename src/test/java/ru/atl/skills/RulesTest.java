package ru.atl.skills;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RulesTest {
    @Test void tierRequirements() {
        assertEquals(new Rules.Requirement(Rules.Skill.PICKAXE, 50), Rules.requirement("minecraft:netherite_pickaxe"));
        assertEquals(new Rules.Requirement(Rules.Skill.SWORD, 30), Rules.requirement("minecraft:diamond_sword"));
    }
    @Test void costsAndBoundaries() {
        for (Rules.Skill skill : Rules.Skill.values()) {
            int expected = skill == Rules.Skill.SWORD ? 1000 : skill == Rules.Skill.HOE ? 250 : 500;
            assertEquals(expected, Rules.threshold(skill, 50) - Rules.threshold(skill, 49));
            for (int level = 1; level < 50; level++) {
                assertEquals(level, Rules.level(skill, Rules.threshold(skill, level)));
                assertEquals(level, Rules.level(skill, Rules.threshold(skill, level + 1) - 1));
                if (level < 49) assertTrue(Rules.cost(skill, level + 1) > Rules.cost(skill, level));
            }
        }
    }
    @Test void preserveOldLevelsAndFraction() {
        for (Rules.Skill skill : Rules.Skill.values()) {
            for (int level = 1; level < 50; level++) {
                int oldStart = 10 * level * (level - 1);
                assertEquals(level, Rules.level(skill, Rules.migrate(skill, oldStart)));
                assertEquals(level, Rules.level(skill, Rules.migrate(skill, oldStart + 10 * level)));
            }
            assertEquals(50, Rules.level(skill, Rules.migrate(skill, 24500)));
        }
    }
    @Test void oreAndMobRewards() {
        assertEquals(1, Rules.pickaxeXp("minecraft:stone"));
        assertEquals(5, Rules.pickaxeXp("minecraft:deepslate_coal_ore"));
        assertEquals(10, Rules.pickaxeXp("minecraft:iron_ore"));
        assertEquals(15, Rules.pickaxeXp("minecraft:nether_gold_ore"));
        assertEquals(20, Rules.pickaxeXp("minecraft:diamond_ore"));
        assertEquals(25, Rules.pickaxeXp("minecraft:emerald_ore"));
        assertEquals(50, Rules.pickaxeXp("minecraft:ancient_debris"));
        assertEquals(20, Rules.mobXp(20));
        assertEquals(0, Rules.mobXp(Float.NaN));
    }
}

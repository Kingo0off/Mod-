package ru.atl.skills;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class VanillaEnchantmentsTest {
    @Test void onlyVanillaEnchantmentsAreShown() {
        assertTrue(VanillaEnchantments.allows("minecraft:unbreaking", "minecraft:diamond_pickaxe"));
        assertTrue(VanillaEnchantments.allows("minecraft:lunge", "minecraft:diamond_spear"));
        assertFalse(VanillaEnchantments.allows("other:berserk", "minecraft:diamond_sword"));
        assertFalse(VanillaEnchantments.allows("minecraft:photosynthesis", "minecraft:diamond_pickaxe"));
        assertFalse(VanillaEnchantments.allows(null, "minecraft:diamond_pickaxe"));
    }
    @Test void pickaxeDoesNotAcceptWeaponEnchantments() {
        assertFalse(VanillaEnchantments.allows("minecraft:sharpness", "minecraft:diamond_pickaxe"));
        assertFalse(VanillaEnchantments.allows("minecraft:smite", "minecraft:iron_pickaxe"));
        assertTrue(VanillaEnchantments.allows("minecraft:efficiency", "minecraft:diamond_pickaxe"));
        assertTrue(VanillaEnchantments.allows("minecraft:sharpness", "minecraft:diamond_sword"));
    }
}

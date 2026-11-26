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
    @Test void protectionIsAvailableForVanillaArmorAndCursesAreExcluded() {
        for (String armor : new String[]{"minecraft:iron_helmet", "minecraft:diamond_chestplate", "minecraft:iron_leggings", "minecraft:netherite_boots"}) {
            assertTrue(VanillaEnchantments.vanillaArmorProtection("minecraft:protection", armor));
            assertTrue(VanillaEnchantments.vanillaArmorProtection("minecraft:fire_protection", armor));
            assertTrue(VanillaEnchantments.vanillaArmorProtection("minecraft:blast_protection", armor));
            assertTrue(VanillaEnchantments.vanillaArmorProtection("minecraft:projectile_protection", armor));
        }
        assertFalse(VanillaEnchantments.vanillaArmorProtection("minecraft:protection", "minecraft:diamond_pickaxe"));
        assertTrue(VanillaEnchantments.priority("minecraft:protection") < VanillaEnchantments.priority("minecraft:fire_protection"));
        assertTrue(VanillaEnchantments.priority("minecraft:fire_protection") < VanillaEnchantments.priority("minecraft:unbreaking"));
        assertFalse(VanillaEnchantments.allows("minecraft:binding_curse", "minecraft:diamond_chestplate"));
        assertFalse(VanillaEnchantments.allows("minecraft:vanishing_curse", "minecraft:diamond_pickaxe"));
    }
    @Test void pickaxeDoesNotAcceptWeaponEnchantments() {
        assertFalse(VanillaEnchantments.allows("minecraft:sharpness", "minecraft:diamond_pickaxe"));
        assertFalse(VanillaEnchantments.allows("minecraft:smite", "minecraft:iron_pickaxe"));
        assertTrue(VanillaEnchantments.allows("minecraft:efficiency", "minecraft:diamond_pickaxe"));
        assertTrue(VanillaEnchantments.allows("minecraft:sharpness", "minecraft:diamond_sword"));
    }
}

package ru.atl.skills;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class ArmorEnchantIntegrationTest {
    @Test void vanillaProtectionFitsAllFourArmorSlots(MinecraftServer server) {
        var registry = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var protection = registry.getOrThrow(Enchantments.PROTECTION);
        var fire = registry.getOrThrow(Enchantments.FIRE_PROTECTION);
        var curse = registry.getOrThrow(Enchantments.BINDING_CURSE);
        for (var item : new net.minecraft.world.item.Item[]{Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS}) {
            var stack = new ItemStack(item);
            assertTrue(EnchantChoices.eligible(stack, protection), item.toString());
            assertTrue(EnchantChoices.eligible(stack, fire), item.toString());
            assertFalse(EnchantChoices.eligible(stack, curse), item.toString());
            stack.enchant(protection, 1);
            assertTrue(EnchantChoices.applicable(stack, fire), "conflicting protection remains visible");
            assertFalse(EnchantChoices.eligible(stack, fire), "protection types must be mutually exclusive");
        }
        assertFalse(EnchantChoices.eligible(new ItemStack(Items.IRON_PICKAXE), protection));
        assertFalse(EnchantChoices.eligible(new ItemStack(Items.IRON_PICKAXE), registry.getOrThrow(Enchantments.SHARPNESS)));
    }
    @Test void unqualifiedEnchantNamesStillRequireTwentyFiveShelves() {
        var id = net.minecraft.resources.Identifier.tryParse("looting");
        assertNotNull(id);
        assertEquals(25, EnchantMath.shelves(id.toString(), 2, false));
    }
    @Test void enchantingTableHasEnoughPositionsForTwentyFiveShelves() {
        assertTrue(net.minecraft.world.level.block.EnchantingTableBlock.BOOKSHELF_OFFSETS.size() >= 25);
    }
    @Test void enchantingMenuHasBookshelfAccessor() {
        assertTrue(ru.atl.skills.mixin.EnchantMenuAccess.class.isAssignableFrom(
                net.minecraft.world.inventory.EnchantmentMenu.class));
    }
    @Test void weaponProgressAndBarMoveTogether(MinecraftServer server) {
        var sharpness = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS);
        var sword = new ItemStack(Items.IRON_SWORD);
        sword.enchant(sharpness, 1);
        int cost = EnchantProgress.cost(sword, 1, sharpness);
        assertEquals(300, cost);
        assertEquals(0, EnchantMath.barFill(EnchantProgress.experience(sword, sharpness), cost, 20));
        for (int i = 0; i < 150; i++) EnchantProgress.use(sword);
        assertEquals(150, EnchantProgress.experience(sword, sharpness));
        assertEquals(10, EnchantMath.barFill(EnchantProgress.experience(sword, sharpness), cost, 20));
        for (int i = 0; i < 150; i++) EnchantProgress.use(sword);
        assertEquals(2, sword.getTagEnchantments().getLevel(sharpness));
        assertEquals(0, EnchantProgress.experience(sword, sharpness));
    }
    @Test void armorExperienceIsIndependentAndUpgradesAtTwenty(MinecraftServer server) {
        var protection = server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
        var first = new ItemStack(Items.IRON_CHESTPLATE);
        var second = new ItemStack(Items.IRON_CHESTPLATE);
        first.enchant(protection, 1);
        second.enchant(protection, 1);
        for (int i = 0; i < 19; i++) EnchantProgress.use(first);
        assertEquals(19, EnchantProgress.experience(first, protection));
        assertEquals(0, EnchantProgress.experience(second, protection));
        assertEquals(1, first.getTagEnchantments().getLevel(protection));
        EnchantProgress.use(first);
        assertEquals(2, first.getTagEnchantments().getLevel(protection));
        assertEquals(1, second.getTagEnchantments().getLevel(protection));
        assertEquals(40, EnchantProgress.cost(first, 2));
    }
}

package ru.atl.skills;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class EnchantChoices {
    private EnchantChoices() {}
    public static boolean eligible(ItemStack item, Holder<Enchantment> enchantment) {
        if (item.isEmpty() || item.getCount() != 1) return false;
        String enchantId = enchantment.unwrapKey().map(key -> key.identifier().toString()).orElse("");
        String itemId = BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
        if (!VanillaEnchantments.allows(enchantId, itemId) || enchantment.is(EnchantmentTags.CURSE)) return false;
        boolean standardProtection = VanillaEnchantments.vanillaArmorProtection(enchantId, itemId)
                && EnchantProgress.isArmor(item);
        if (!standardProtection && (!item.supportsEnchantment(enchantment)
                || !enchantment.value().isSupportedItem(item))) return false;
        ItemEnchantments enchants = item.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchants.getLevel(enchantment) != 0) return false;
        return enchants.keySet().stream().allMatch(old -> Enchantment.areCompatible(old, enchantment));
    }
    public static List<Holder<Enchantment>> options(Player player, ItemStack item) {
        if (item.isEmpty()) return List.of();
        return player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .filter(holder -> eligible(item, holder))
                .sorted(Comparator.<net.minecraft.core.Holder.Reference<Enchantment>>comparingInt(holder -> VanillaEnchantments.priority(holder.key().identifier().toString()))
                        .thenComparing(holder -> holder.key().identifier().toString()))
                .map(holder -> (Holder<Enchantment>)holder).toList();
    }
}

package ru.atl.skills;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** Progress is stored on the stack; enchantments already at their maximum are never altered. */
public final class EnchantProgress {
    private static final String KEY = "atl_skills:enchant_progress";
    private EnchantProgress() {}
    public static int cost(int level) { return Math.min(100, Math.max(1, level)) * 100; }
    public record Advance(int level, int experience) {}
    public static Advance advance(int level, int maximum, int experience) {
        if (level < 1 || level >= maximum) return new Advance(level, 0);
        int xp = Math.max(0, Math.min(cost(level), experience));
        return xp + 1 >= cost(level) ? new Advance(level + 1, 0) : new Advance(level, xp + 1);
    }
    private static String id(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey().map(key -> key.identifier().toString()).orElse("");
    }
    public static int experience(ItemStack stack, Holder<Enchantment> enchantment) {
        String id = id(enchantment);
        if (id.isEmpty()) return 0;
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                .getCompoundOrEmpty(KEY).getIntOr(id, 0);
    }
    public static void use(ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemEnchantments existing = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (existing.isEmpty()) return;
        ItemEnchantments.Mutable changed = new ItemEnchantments.Mutable(existing);
        CompoundTag progress = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompoundOrEmpty(KEY);
        boolean updated = false;
        for (Holder<Enchantment> enchantment : existing.keySet()) {
            String id = id(enchantment);
            int level = existing.getLevel(enchantment);
            if (id.isEmpty() || level < 1 || level >= enchantment.value().getMaxLevel()) continue;
            Advance next = advance(level, enchantment.value().getMaxLevel(), progress.getIntOr(id, 0));
            if (next.level() != level) changed.set(enchantment, next.level());
            progress.putInt(id, next.experience());
            updated = true;
        }
        if (!updated) return;
        stack.set(DataComponents.ENCHANTMENTS, changed.toImmutable());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.put(KEY, progress));
    }
}

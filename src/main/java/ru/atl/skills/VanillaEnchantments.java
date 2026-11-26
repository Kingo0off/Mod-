package ru.atl.skills;

import java.util.Set;

/** Minecraft 26.3 enchantment IDs; datapacks and other mods cannot add entries to the table. */
public final class VanillaEnchantments {
    private VanillaEnchantments() {}
    private static final Set<String> VANILLA = Set.of(
        "protection", "fire_protection", "feather_falling", "blast_protection", "projectile_protection",
        "respiration", "aqua_affinity", "thorns", "depth_strider", "frost_walker",
        "soul_speed", "swift_sneak", "sharpness", "smite", "bane_of_arthropods", "knockback",
        "fire_aspect", "looting", "sweeping_edge", "efficiency", "silk_touch", "unbreaking", "fortune",
        "power", "punch", "flame", "infinity", "luck_of_the_sea", "lure", "loyalty", "impaling",
        "riptide", "channeling", "multishot", "quick_charge", "piercing", "density", "breach",
        "wind_burst", "lunge", "mending"
    );
    private static final Set<String> PICKAXE = Set.of("efficiency", "silk_touch", "unbreaking", "fortune", "mending");
    public static boolean isProtection(String enchantId) {
        return "minecraft:protection".equals(enchantId) || "minecraft:fire_protection".equals(enchantId)
            || "minecraft:blast_protection".equals(enchantId) || "minecraft:projectile_protection".equals(enchantId);
    }
    public static int priority(String enchantId) {
        return "minecraft:protection".equals(enchantId) ? 0 : isProtection(enchantId) ? 1 : 2;
    }
    public static boolean vanillaArmorProtection(String enchantId, String itemId) {
        if (!isProtection(enchantId) || itemId == null || !itemId.startsWith("minecraft:")) return false;
        return itemId.endsWith("_helmet") || itemId.endsWith("_chestplate")
            || itemId.endsWith("_leggings") || itemId.endsWith("_boots");
    }
    public static boolean allows(String enchantId, String itemId) {
        if (enchantId == null || !enchantId.startsWith("minecraft:")) return false;
        String name = enchantId.substring("minecraft:".length());
        if (!VANILLA.contains(name)) return false;
        return itemId == null || !itemId.endsWith("_pickaxe") || PICKAXE.contains(name);
    }
}

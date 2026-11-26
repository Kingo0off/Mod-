package ru.atl.skills;

import java.util.Set;

/** Minecraft 26.3 enchantment IDs; datapacks and other mods cannot add entries to the table. */
public final class VanillaEnchantments {
    private VanillaEnchantments() {}
    private static final Set<String> VANILLA = Set.of(
        "protection", "fire_protection", "feather_falling", "blast_protection", "projectile_protection",
        "respiration", "aqua_affinity", "thorns", "depth_strider", "frost_walker", "binding_curse",
        "soul_speed", "swift_sneak", "sharpness", "smite", "bane_of_arthropods", "knockback",
        "fire_aspect", "looting", "sweeping_edge", "efficiency", "silk_touch", "unbreaking", "fortune",
        "power", "punch", "flame", "infinity", "luck_of_the_sea", "lure", "loyalty", "impaling",
        "riptide", "channeling", "multishot", "quick_charge", "piercing", "density", "breach",
        "wind_burst", "lunge", "mending", "vanishing_curse"
    );
    private static final Set<String> PICKAXE = Set.of("efficiency", "silk_touch", "unbreaking", "fortune", "mending", "vanishing_curse");
    public static boolean allows(String enchantId, String itemId) {
        if (enchantId == null || !enchantId.startsWith("minecraft:")) return false;
        String name = enchantId.substring("minecraft:".length());
        if (!VANILLA.contains(name)) return false;
        return itemId == null || !itemId.endsWith("_pickaxe") || PICKAXE.contains(name);
    }
}

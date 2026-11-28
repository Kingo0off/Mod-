package ru.atl.skills;
public final class EnchantMath {
    private EnchantMath() {}
    public static int cost(int level) { return cost(level, false); }
    public static int cost(int level, boolean armor) { return Math.min(100, Math.max(1, level)) * (armor ? 20 : 300); }
    public static int rarity(int weight) { return weight >= 10 ? 1 : weight >= 5 ? 2 : weight >= 2 ? 3 : 4; }
    public static int shelves(int weight) { return switch (rarity(weight)) { case 1 -> 0; case 2 -> 5; case 3 -> 10; default -> 15; }; }
    public static int levels(int weight) { return switch (rarity(weight)) { case 1 -> 1; case 2 -> 5; case 3 -> 10; default -> 15; }; }
    public static int shelves(String id, int weight, boolean armor) {
        if (!armor && ("minecraft:fortune".equals(id) || "minecraft:looting".equals(id)
                || "minecraft:silk_touch".equals(id) || "minecraft:infinity".equals(id)
                || "minecraft:mending".equals(id) || "minecraft:wind_burst".equals(id))) return 25;
        if (!armor && "minecraft:sharpness".equals(id)) return 10;
        return shelves(weight);
    }
    public static int levels(String id, int weight, boolean armor) {
        if (!armor && "minecraft:sharpness".equals(id)) return 10;
        return levels(weight);
    }
    public static int barFill(int xp, int cost, int segments) {
        if (cost <= 0 || segments <= 0) return 0;
        return (int)Math.min(segments, Math.max(0L, (long)Math.max(0, xp) * segments / cost));
    }
    public static int cost(int level, boolean armor, int weight) { return cost(level, armor) * rarity(weight); }
    public record Advance(int level, int experience) {}
    public static Advance advance(int level, int maximum, int experience) { return advance(level, maximum, experience, false); }
    public static Advance advance(int level, int maximum, int experience, boolean armor) {
        return advance(level, maximum, experience, armor, 10);
    }
    public static Advance advance(int level, int maximum, int experience, boolean armor, int weight) {
        if (level < 1 || level >= maximum) return new Advance(level, 0);
        int xp = Math.max(0, experience);
        int needed = cost(level, armor, weight);
        if (xp >= needed - 1) {
            int carry = (int)Math.min(Integer.MAX_VALUE, (long)xp + 1 - needed);
            return new Advance(level + 1, level + 1 >= maximum ? 0 : carry);
        }
        return new Advance(level, xp + 1);
    }
}

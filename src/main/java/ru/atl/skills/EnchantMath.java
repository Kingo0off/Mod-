package ru.atl.skills;
public final class EnchantMath {
    private EnchantMath() {}
    public static int cost(int level) { return Math.min(100, Math.max(1, level)) * 100; }
    public record Advance(int level, int experience) {}
    public static Advance advance(int level, int maximum, int experience) {
        if (level < 1 || level >= maximum) return new Advance(level, 0);
        int xp = Math.max(0, Math.min(cost(level), experience));
        return xp + 1 >= cost(level) ? new Advance(level + 1, 0) : new Advance(level, xp + 1);
    }
}

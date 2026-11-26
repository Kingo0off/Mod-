package ru.atl.skills;
public final class EnchantMath {
    private EnchantMath() {}
    public static int cost(int level) { return cost(level, false); }
    public static int cost(int level, boolean armor) { return Math.min(100, Math.max(1, level)) * (armor ? 20 : 100); }
    public record Advance(int level, int experience) {}
    public static Advance advance(int level, int maximum, int experience) { return advance(level, maximum, experience, false); }
    public static Advance advance(int level, int maximum, int experience, boolean armor) {
        if (level < 1 || level >= maximum) return new Advance(level, 0);
        int xp = Math.max(0, experience);
        int needed = cost(level, armor);
        if (xp >= needed - 1) {
            int carry = (int)Math.min(Integer.MAX_VALUE, (long)xp + 1 - needed);
            return new Advance(level + 1, level + 1 >= maximum ? 0 : carry);
        }
        return new Advance(level, xp + 1);
    }
}

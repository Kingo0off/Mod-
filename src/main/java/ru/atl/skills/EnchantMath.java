package ru.atl.skills;
public final class EnchantMath {
    private EnchantMath() {}
    public static int cost(int level) { return cost(level, false); }
    public static int cost(int level, boolean armor) { return Math.min(100, Math.max(1, level)) * (armor ? 20 : 100); }
    public static int rarity(int weight) { return weight >= 10 ? 1 : weight >= 5 ? 2 : weight >= 2 ? 3 : 4; }
    public static int shelves(int weight) { return switch (rarity(weight)) { case 1 -> 0; case 2 -> 5; case 3 -> 10; default -> 15; }; }
    public static int levels(int weight) { return switch (rarity(weight)) { case 1 -> 1; case 2 -> 5; case 3 -> 10; default -> 15; }; }
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

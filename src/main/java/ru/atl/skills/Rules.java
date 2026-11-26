package ru.atl.skills;

public final class Rules {
    private Rules() {}
    public enum Skill {
        PICKAXE("Кирка", "pickaxe"), AXE("Топор", "axe"), SHOVEL("Лопата", "shovel"), HOE("Мотыга", "hoe"), SWORD("Меч", "sword");
        public final String label, suffix;
        Skill(String label, String suffix) { this.label = label; this.suffix = suffix; }
    }
    public record Requirement(Skill skill, int level) {}
    public static Requirement requirement(String id) {
        if (id == null || !id.startsWith("minecraft:")) return null;
        String path = id.substring("minecraft:".length());
        int i = path.indexOf('_'); if (i < 0) return null;
        int required = switch (path.substring(0, i)) {
            case "wooden" -> 1; case "stone" -> 5; case "copper" -> 10;
            case "iron" -> 15; case "golden" -> 20; case "diamond" -> 30; case "netherite" -> 50; default -> 0;
        };
        if (required == 0) return null;
        String suffix = path.substring(i + 1);
        for (Skill s : Skill.values()) if (s.suffix.equals(suffix)) return new Requirement(s, required);
        return null;
    }
    public static int cost(Skill skill, int currentLevel) {
        int finalCost = skill == Skill.SWORD ? 1000 : skill == Skill.HOE ? 250 : 500;
        return (finalCost * Math.max(1, Math.min(49, currentLevel)) + 48) / 49;
    }
    public static int threshold(Skill skill, int level) {
        int total = 0;
        for (int i = 1; i < Math.min(50, level); i++) total += cost(skill, i);
        return total;
    }
    public static int level(Skill skill, int xp) {
        int level = 1;
        while (level < 50 && xp >= threshold(skill, level + 1)) level++;
        return level;
    }
    public static int migrate(Skill skill, int xp) {
        int oldLevel = 1;
        while (oldLevel < 50 && xp >= 10 * (oldLevel + 1) * oldLevel) oldLevel++;
        if (oldLevel >= 50) return threshold(skill, 50);
        int oldStart = 10 * oldLevel * (oldLevel - 1);
        int oldCost = 20 * oldLevel;
        int oldProgress = Math.max(0, xp - oldStart);
        return threshold(skill, oldLevel) + (int)((long) oldProgress * cost(skill, oldLevel) / oldCost);
    }
    public static int mobXp(float maxHealth) {
        if (!Float.isFinite(maxHealth) || maxHealth <= 0) return 0;
        return Math.max(1, Math.min(threshold(Skill.SWORD, 50), Math.round(maxHealth)));
    }
    public static int pickaxeXp(String block) {
        if (block == null) return 1;
        return switch (block) {
            case "minecraft:coal_ore", "minecraft:deepslate_coal_ore" -> 5;
            case "minecraft:iron_ore", "minecraft:deepslate_iron_ore" -> 10;
            case "minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore" -> 15;
            case "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore" -> 20;
            case "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore" -> 25;
            case "minecraft:ancient_debris" -> 50;
            default -> 1;
        };
    }
}

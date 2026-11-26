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
    public static int level(int xp) {
        int level = 1; while (level < 50 && xp >= threshold(level + 1)) level++; return level;
    }
    public static int threshold(int level) { return 10 * level * (level - 1); }
}

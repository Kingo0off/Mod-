package ru.atl.skills.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import ru.atl.skills.AtlSkills;
import ru.atl.skills.Rules;

public final class SkillsScreen extends Screen {
    public SkillsScreen() { super(Component.literal("Навыки")); }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int w = Math.min(340, width - 20);
        int h = Math.min(224, height - 16);
        if (w < 170 || h < 140) { super.extractRenderState(g, mouseX, mouseY, partialTick); return; }
        int x = (width - w) / 2, y = (height - h) / 2;
        g.fill(0, 0, width, height, 0xB5000000);
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF6650A9);
        g.fill(x, y, x + w, y + h, 0xF5191727);
        g.fill(x, y, x + w, y + 3, 0xFFA47CFF);
        g.text(font, "ATL SKILLS", x + 14, y + 12, 0xFFC1ACFF, false);
        g.text(font, "Навыки игрока", x + 14, y + 27, 0xFFFFFFFF, false);
        g.text(font, "ESC — закрыть", x + w - 91, y + 14, 0xFFAAA4BB, false);
        int rowTop = y + 44;
        int rowH = (h - 54) / Rules.Skill.values().length;
        Player p = Minecraft.getInstance().player;
        int index = 0;
        for (Rules.Skill skill : Rules.Skill.values()) {
            int top = rowTop + index++ * rowH;
            g.fill(x + 10, top, x + w - 10, top + rowH - 3, 0xFF28243B);
            g.fill(x + 10, top, x + 13, top + rowH - 3, 0xFF8C6BE4);
            Integer xp = p == null ? null : AtlSkills.skillXp(p, skill);
            int textX = x + 41, right = x + w - 19;
            int level = xp == null ? 1 : Rules.level(xp);
            g.item(SkillsClient.icon(skill, level), x + 17, top + (rowH - 19) / 2);
            String label = xp == null ? "Загрузка…" : rowH < 34 && level < 50 ? "Ур. " + level + " · −" + Math.max(0, Rules.threshold(level + 1) - xp) + " XP" : "Уровень " + level;
            g.text(font, skill.label, textX, top + 4, 0xFFF4F0FF, false);
            g.text(font, label, right - font.width(label), top + 4, 0xFFC7B5FF, false);
            if (xp != null && rowH >= 34) {
                String remaining = level >= 50 ? "Максимальный уровень" : "До ур. " + (level + 1) + ": " + Math.max(0, Rules.threshold(level + 1) - xp) + " XP";
                g.text(font, remaining, textX, top + 15, 0xFFAAA4BB, false);
            }
            int barY = top + rowH - 10, barWidth = Math.max(0, right - textX);
            g.fill(textX, barY, right, barY + 3, 0xFF514A63);
            int current = xp == null ? 0 : level >= 50 ? 1 : Math.max(0, xp - Rules.threshold(level));
            int total = xp == null || level >= 50 ? 1 : Rules.threshold(level + 1) - Rules.threshold(level);
            int fill = Math.max(0, Math.min(barWidth, (int)((long)barWidth * current / total)));
            if (fill > 0) g.fill(textX, barY, textX + fill, barY + 3, 0xFFA788FF);
        }
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    }
}

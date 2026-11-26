package ru.atl.skills.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.network.chat.Component;
import ru.atl.skills.AtlSkills;
import ru.atl.skills.Rules;

@EventBusSubscriber(modid = AtlSkills.ID, value = Dist.CLIENT)
public final class SkillsClient {
    private static final KeyMapping OPEN = new KeyMapping("key.atl_skills.open", InputConstants.KEY_K, KeyMapping.Category.MISC);
    private static final Map<Rules.Skill, Integer> LAST = new EnumMap<>(Rules.Skill.class);
    private static final Queue<Notice> QUEUE = new ArrayDeque<>();
    private static Player previousPlayer;
    private static Notice notice;
    private static long noticeUntil;
    private record Notice(Rules.Skill skill, int level) {}

    private SkillsClient() {}

    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) { event.register(OPEN); }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != previousPlayer) {
            previousPlayer = player; LAST.clear(); QUEUE.clear(); notice = null; noticeUntil = 0;
        }
        if (player == null) { while (OPEN.consumeClick()) {} return; }
        while (OPEN.consumeClick()) {
            if (mc.gui.screen() == null) mc.gui.setScreen(new SkillsScreen());
        }
        for (Rules.Skill skill : Rules.Skill.values()) {
            Integer xp = AtlSkills.skillXp(player, skill);
            if (xp == null) continue;
            int level = Rules.level(xp);
            Integer old = LAST.put(skill, level);
            if (old != null && level > old) {
                if (QUEUE.size() < 10) QUEUE.add(new Notice(skill, level));
            }
        }
        long now = System.nanoTime();
        if (notice != null && now >= noticeUntil) notice = null;
        if (notice == null && !QUEUE.isEmpty()) {
            notice = QUEUE.remove();
            noticeUntil = now + 4_000_000_000L;
        }
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        Rules.Requirement req = AtlSkills.requirement(event.getItemStack());
        if (req == null) return;
        Player p = event.getEntity();
        Integer xp = p == null ? null : AtlSkills.skillXp(p, req.skill());
        ChatFormatting color = xp == null ? ChatFormatting.GRAY : Rules.level(xp) >= req.level() ? ChatFormatting.GREEN : ChatFormatting.RED;
        event.getToolTip().add(Component.literal("Требуется: " + req.skill().label + " — ур. " + req.level()).withStyle(color));
    }

    static ItemStack icon(Rules.Skill skill) {
        return new ItemStack(switch (skill) {
            case PICKAXE -> Items.IRON_PICKAXE;
            case AXE -> Items.IRON_AXE;
            case SHOVEL -> Items.IRON_SHOVEL;
            case HOE -> Items.IRON_HOE;
            case SWORD -> Items.IRON_SWORD;
        });
    }

    @SubscribeEvent public static void hud(RenderGuiEvent.Post event) {
        if (notice == null || Minecraft.getInstance().player == null) return;
        GuiGraphicsExtractor g = event.getGuiGraphics();
        int w = Math.min(240, g.guiWidth() - 12), x = (g.guiWidth() - w) / 2, y = 12;
        if (w < 100) return;
        g.nextStratum();
        g.fill(x - 1, y - 1, x + w + 1, y + 39, 0xFF7759D8);
        g.fill(x, y, x + w, y + 38, 0xF31C192D);
        g.fill(x, y, x + 3, y + 38, 0xFFAD8CFF);
        g.fill(x + 9, y + 9, x + 29, y + 29, 0xFF342752);
        g.item(icon(notice.skill()), x + 11, y + 11);
        g.text(Minecraft.getInstance().font, "НОВЫЙ УРОВЕНЬ", x + 36, y + 7, 0xFFBCA4FF, false);
        g.text(Minecraft.getInstance().font, notice.skill().label + " · уровень " + notice.level(), x + 36, y + 21, 0xFFFFFFFF, false);
    }
}

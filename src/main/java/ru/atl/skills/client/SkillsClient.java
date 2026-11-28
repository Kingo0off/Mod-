package ru.atl.skills.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
import ru.atl.skills.EnchantProgress;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import ru.atl.skills.Rules;

@EventBusSubscriber(modid = AtlSkills.ID, value = Dist.CLIENT)
public final class SkillsClient {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(AtlSkills.ID, "controls"));
    private static final KeyMapping OPEN = new KeyMapping("key.atl_skills.open", InputConstants.KEY_K, CATEGORY);
    private static final Map<Rules.Skill, Integer> LAST = new EnumMap<>(Rules.Skill.class);
    private static final Queue<Notice> QUEUE = new ArrayDeque<>();
    private static Player previousPlayer;
    private static Notice notice;
    private static long noticeUntil;
    private static long noticeStart;
    private record Notice(Rules.Skill skill, int level) {}

    private SkillsClient() {}

    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) { event.registerCategory(CATEGORY); event.register(OPEN); }

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
            int level = Rules.level(skill, xp);
            Integer old = LAST.put(skill, level);
            if (old != null && level > old) {
                if (QUEUE.size() < 10) QUEUE.add(new Notice(skill, level));
            }
        }
        long now = System.nanoTime();
        if (notice != null && now >= noticeUntil) notice = null;
        if (notice == null && !QUEUE.isEmpty()) {
            notice = QUEUE.remove();
            noticeStart = now;
            noticeUntil = now + 4_000_000_000L;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Rules.Requirement req = AtlSkills.requirement(stack);
        if (req != null) {
            Player p = event.getEntity();
            Integer xp = p == null ? null : AtlSkills.skillXp(p, req.skill());
            ChatFormatting color = xp == null ? ChatFormatting.GRAY : Rules.level(req.skill(), xp) >= req.level() ? ChatFormatting.GREEN : ChatFormatting.RED;
            event.getToolTip().add(Component.literal("Требуется: " + req.skill().label + " — ур. " + req.level()).withStyle(color));
        }
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        boolean hasProgress = enchants.keySet().stream().anyMatch(e -> enchants.getLevel(e) < e.value().getMaxLevel());
        if (!hasProgress) return;
        if (!event.getFlags().hasShiftDown()) {
            event.getToolTip().add(Component.literal("[Shift] Показать прогресс чар").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        for (var enchantment : enchants.keySet()) {
            int level = enchants.getLevel(enchantment);
            if (level >= enchantment.value().getMaxLevel()) continue;
            int cost = EnchantProgress.cost(stack, level, enchantment);
            int xp = Math.max(0, EnchantProgress.experience(stack, enchantment));
            int filled = Math.min(10, (int)((long)xp * 10 / cost));
            event.getToolTip().add(Enchantment.getFullname(enchantment, level).copy()
                    .append(Component.literal("  " + "■".repeat(filled) + "□".repeat(10 - filled) + " " + xp + "/" + cost + " (осталось " + Math.max(0, cost - xp) + ")")
                            .withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
    }

    static ItemStack icon(Rules.Skill skill, int level) {
        String material = level >= 50 ? "netherite" : level >= 30 ? "diamond" : level >= 20 ? "golden" : level >= 15 ? "iron" : level >= 10 ? "copper" : level >= 5 ? "stone" : "wooden";
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", material + "_" + skill.suffix)));
    }

    @SubscribeEvent public static void hud(RenderGuiEvent.Post event) {
        if (notice == null || Minecraft.getInstance().player == null) return;
        GuiGraphicsExtractor g = event.getGuiGraphics();
        int w = Math.min(240, g.guiWidth() - 12), x = (g.guiWidth() - w) / 2;
        long now = System.nanoTime();
        double progress = Math.min(1.0, Math.min((now - noticeStart) / 450_000_000.0, (noticeUntil - now) / 450_000_000.0));
        progress = Math.max(0.0, progress);
        progress = progress * progress * (3.0 - 2.0 * progress);
        int y = (int)Math.round(-40 + 52 * progress);
        if (w < 100) return;
        g.nextStratum();
        g.fill(x - 1, y - 1, x + w + 1, y + 39, 0xFF7759D8);
        g.fill(x, y, x + w, y + 38, 0xF31C192D);
        g.fill(x, y, x + 3, y + 38, 0xFFAD8CFF);
        g.fill(x + 9, y + 9, x + 29, y + 29, 0xFF342752);
        g.item(icon(notice.skill(), notice.level()), x + 11, y + 11);
        g.text(Minecraft.getInstance().font, "НОВЫЙ УРОВЕНЬ", x + 36, y + 7, 0xFFBCA4FF, false);
        g.text(Minecraft.getInstance().font, notice.skill().label + " · уровень " + notice.level(), x + 36, y + 21, 0xFFFFFFFF, false);
    }
}

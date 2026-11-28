package ru.atl.skills.client;

import java.util.List;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import ru.atl.skills.AtlSkills;
import ru.atl.skills.ChooseEnchant;
import ru.atl.skills.EnchantChoices;
import ru.atl.skills.EnchantMath;
import ru.atl.skills.EnchantProgress;
import ru.atl.skills.ShelfState;

@EventBusSubscriber(modid = AtlSkills.ID, value = Dist.CLIENT)
public final class EnchantPanel {
    private static int offset;
    private static EnchantmentScreen current;
    private static AbstractContainerMenu syncedMenu;
    private static int shelfCount = -1;
    private EnchantPanel() {}
    @SubscribeEvent public static void payloads(RegisterClientPayloadHandlersEvent event) {
        event.register(ShelfState.TYPE, (payload, context) -> {
            var player = Minecraft.getInstance().player;
            if (player != null && player.containerMenu.containerId == payload.containerId()) {
                syncedMenu = player.containerMenu;
                shelfCount = payload.count();
            }
        });
    }
    private static List<Holder<Enchantment>> choices(EnchantmentScreen screen) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? List.of() : EnchantChoices.options(mc.player, screen.getMenu().getSlot(0).getItem());
    }
    private static int x(EnchantmentScreen screen) { return (screen.width - 176) / 2 + 59; }
    private static int y(EnchantmentScreen screen) { return (screen.height - 166) / 2 + 14; }
    private static boolean inside(EnchantmentScreen s, double x, double y) {
        return x >= x(s) && x < x(s) + 109 && y >= y(s) && y < y(s) + 58;
    }
    @SubscribeEvent public static void render(ScreenEvent.Render.Foreground event) {
        if (!(event.getScreen() instanceof EnchantmentScreen s)) return;
        if (s != current) { current = s; offset = 0; }
        var options = choices(s);
        offset = Math.max(0, Math.min(offset, Math.max(0, options.size() - 3)));
        GuiGraphicsExtractor g = event.getGuiGraphics();
        int x = x(s), y = y(s);
        int availableShelves = syncedMenu == s.getMenu() ? shelfCount : -1;
        g.fill(x - 1, y - 1, x + 110, y + 59, 0xFF25202B);
        g.fill(x, y, x + 109, y + 58, 0xFF615746);
        var font = Minecraft.getInstance().font;
        g.text(font, "Полки: " + (availableShelves < 0 ? "…" : availableShelves), x + 2, y - 9, 0xFFD5B963, false);
        if (options.isEmpty()) {
            g.text(font, "Нет доступных", x + 4, y + 6, 0xFFD3C6AA, false);
            g.text(font, "зачарований", x + 4, y + 17, 0xFFD3C6AA, false);
        }
        for (int row = 0; row < 3 && offset + row < options.size(); row++) {
            int top = y + row * 19;
            Holder<Enchantment> enchantment = options.get(offset + row);
            boolean allowed = EnchantChoices.eligible(s.getMenu().getSlot(0).getItem(), enchantment);
            boolean hovered = event.getMouseX() >= x && event.getMouseX() < x + 109
                    && event.getMouseY() >= top && event.getMouseY() < top + 19;
            g.fill(x + 1, top + 1, x + 108, top + 18,
                    !allowed ? 0xFF373239 : hovered ? 0xFFAC6FA4 : 0xFFA99873);
            String name = Enchantment.getFullname(enchantment, 1).getString();
            int weight = enchantment.value().getWeight();
            String id = enchantment.unwrapKey().map(key -> key.identifier().toString()).orElse("");
            boolean armor = EnchantProgress.isArmor(s.getMenu().getSlot(0).getItem());
            int neededShelves = EnchantMath.shelves(id, weight, armor), levels = EnchantMath.levels(id, weight, armor);
            g.text(font, font.plainSubstrByWidth(name, 67), x + 4, top + 5, allowed ? 0xFF271F1B : 0xFF817983, false);
            String shelfText = Integer.toString(neededShelves), levelText = Integer.toString(levels);
            g.text(font, shelfText, x + 89 - font.width(shelfText), top + 5, allowed ? 0xFFFFC14D : 0xFF877454, false);
            g.text(font, levelText, x + 105 - font.width(levelText), top + 5, allowed ? 0xFF58E342 : 0xFF627B5B, false);

        }
        if (offset > 0) g.text(font, "↑", x + 99, y + 1, 0xFFFFFFFF, false);
        if (offset + 3 < options.size()) g.text(font, "↓", x + 99, y + 47, 0xFFFFFFFF, false);
    }
    @SubscribeEvent public static void hover(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof EnchantmentScreen s) || !inside(s, event.getMouseX(), event.getMouseY())) return;
        var options = choices(s);
        int index = offset + (event.getMouseY() - y(s)) / 19;
        if (index < 0 || index >= options.size()) return;
        var enchantment = options.get(index);
        boolean allowed = EnchantChoices.eligible(s.getMenu().getSlot(0).getItem(), enchantment);
        int weight = enchantment.value().getWeight();
        String id = enchantment.unwrapKey().map(key -> key.identifier().toString()).orElse("");
        boolean armor = EnchantProgress.isArmor(s.getMenu().getSlot(0).getItem());
        event.getGuiGraphics().setComponentTooltipForNextFrame(Minecraft.getInstance().font, List.of(
                Enchantment.getFullname(enchantment, 1),
                Component.literal(EnchantDescriptions.description(id)),
                Component.literal("§eПолки: " + EnchantMath.shelves(id, weight, armor) + " §aУровни: " + EnchantMath.levels(id, weight, armor)),
                Component.literal(allowed ? "§7Стоимость: также 1 лазурит" : "§cКонфликтует с чарами предмета или уже установлено")
        ), event.getMouseX(), event.getMouseY());
    }
    @SubscribeEvent public static void click(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof EnchantmentScreen s) || !inside(s, event.getMouseX(), event.getMouseY())) return;
        event.setCanceled(true);
        if (event.getButton() != InputConstants.MOUSE_BUTTON_LEFT) return;
        var options = choices(s);
        int index = offset + (int)(event.getMouseY() - y(s)) / 19;
        if (index < 0 || index >= options.size()) return;
        Holder<Enchantment> enchantment = options.get(index);
        if (!EnchantChoices.eligible(s.getMenu().getSlot(0).getItem(), enchantment)) return;
        enchantment.unwrapKey().ifPresent(key -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ClientPacketDistributor.sendToServer(new ChooseEnchant(s.getMenu().containerId, key.identifier().toString()));
        });
    }
    @SubscribeEvent public static void scroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof EnchantmentScreen s) || !inside(s, event.getMouseX(), event.getMouseY())) return;
        event.setCanceled(true);
        offset = Math.max(0, Math.min(Math.max(0, choices(s).size() - 3), offset + (event.getScrollDeltaY() < 0 ? 1 : -1)));
    }
}

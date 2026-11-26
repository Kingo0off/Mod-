package ru.atl.skills.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import ru.atl.skills.AtlSkills;
import ru.atl.skills.ChooseEnchant;
import ru.atl.skills.EnchantChoices;

@EventBusSubscriber(modid = AtlSkills.ID, value = Dist.CLIENT)
public final class EnchantPanel {
    private static int offset;
    private static EnchantmentScreen current;
    private EnchantPanel() {}
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
        g.fill(x, y, x + 109, y + 58, 0xFF211B32);
        var font = Minecraft.getInstance().font;
        if (options.isEmpty()) {
            g.text(font, "Нет доступных", x + 4, y + 6, 0xFFB9AACF, false);
            g.text(font, "зачарований", x + 4, y + 17, 0xFFB9AACF, false);
        }
        for (int row = 0; row < 3 && offset + row < options.size(); row++) {
            int top = y + row * 19;
            g.fill(x + 1, top + 1, x + 108, top + 18, 0xFF3C3056);
            String name = Enchantment.getFullname(options.get(offset + row), 1).getString();
            g.text(font, font.plainSubstrByWidth(name, 101), x + 4, top + 5, 0xFFE6DCFF, false);
        }
    }
    @SubscribeEvent public static void click(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof EnchantmentScreen s) || !inside(s, event.getMouseX(), event.getMouseY())) return;
        event.setCanceled(true);
        if (event.getButton() != 0) return;
        var options = choices(s);
        int index = offset + (int)(event.getMouseY() - y(s)) / 19;
        if (index < 0 || index >= options.size()) return;
        options.get(index).unwrapKey().ifPresent(key ->
            ClientPacketDistributor.sendToServer(new ChooseEnchant(s.getMenu().containerId, key.identifier().toString())));
    }
    @SubscribeEvent public static void scroll(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof EnchantmentScreen s) || !inside(s, event.getMouseX(), event.getMouseY())) return;
        event.setCanceled(true);
        offset = Math.max(0, Math.min(Math.max(0, choices(s).size() - 3), offset + (event.getScrollDeltaY() < 0 ? 1 : -1)));
    }
}

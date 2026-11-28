package ru.atl.skills;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChooseEnchant(int containerId, String enchantment) implements CustomPacketPayload {
    public static final Type<ChooseEnchant> TYPE = new Type<>(Identifier.fromNamespaceAndPath(AtlSkills.ID, "choose_enchant"));
    public static final StreamCodec<ByteBuf, ChooseEnchant> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChooseEnchant::containerId,
            ByteBufCodecs.STRING_UTF8, ChooseEnchant::enchantment, ChooseEnchant::new);
    @Override public Type<ChooseEnchant> type() { return TYPE; }

    @EventBusSubscriber(modid = AtlSkills.ID)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
            var registrar = event.registrar("2");
            registrar.playToServer(TYPE, CODEC, ChooseEnchant::handle);
            registrar.playToClient(ShelfState.TYPE, ShelfState.CODEC);
        }
    }
    private static void handle(ChooseEnchant packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof EnchantmentMenu menu)
                || menu.containerId != packet.containerId() || !menu.stillValid(player)) return;
        Identifier id = Identifier.tryParse(packet.enchantment());
        if (id == null) return;
        var key = ResourceKey.create(Registries.ENCHANTMENT, id);
        var holder = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
        if (holder == null) return;
        ItemStack item = menu.getSlot(0).getItem();
        ItemStack lapis = menu.getSlot(1).getItem();
        boolean creative = player.getAbilities().instabuild;
        if (!EnchantChoices.eligible(item, holder)) {
            player.sendOverlayMessage(Component.literal("Это зачарование не подходит предмету").withStyle(ChatFormatting.RED));
            return;
        }
        int neededLevels = EnchantMath.levels(holder.value().getWeight());
        int neededShelves = EnchantMath.shelves(holder.value().getWeight());
        if (!creative && ShelfState.count(menu) < neededShelves) {
            player.sendOverlayMessage(Component.literal("Нужно книжных полок: " + neededShelves).withStyle(ChatFormatting.RED));
            return;
        }
        if (!creative && (player.experienceLevel < neededLevels || !lapis.is(Items.LAPIS_LAZULI) || lapis.getCount() < 1)) {
            player.sendOverlayMessage(Component.literal("Нужны уровни: " + neededLevels + " и 1 лазурит").withStyle(ChatFormatting.RED));
            return;
        }
        item.enchant(holder, 1);
        player.onEnchantmentPerformed(item, neededLevels);
        if (!creative) { lapis.shrink(1); menu.getSlot(1).setChanged(); }
        menu.getSlot(0).setChanged();
        menu.broadcastChanges();
    }
}

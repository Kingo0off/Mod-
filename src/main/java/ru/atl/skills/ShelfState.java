package ru.atl.skills;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.atl.skills.mixin.EnchantMenuAccess;

public record ShelfState(int containerId, int count) implements CustomPacketPayload {
    public static final Type<ShelfState> TYPE = new Type<>(Identifier.fromNamespaceAndPath(AtlSkills.ID, "shelf_state"));
    public static final StreamCodec<ByteBuf, ShelfState> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ShelfState::containerId, ByteBufCodecs.VAR_INT, ShelfState::count, ShelfState::new);
    @Override public Type<ShelfState> type() { return TYPE; }
    public static int count(EnchantmentMenu menu) {
        return ((EnchantMenuAccess)menu).atl$getAccess().evaluate((level, pos) -> {
            int count = 0;
            for (var offset : EnchantingTableBlock.BOOKSHELF_OFFSETS)
                if (EnchantingTableBlock.isValidBookShelf(level, pos, offset)) count++;
            return count;
        }).orElse(0);
    }
    @EventBusSubscriber(modid = AtlSkills.ID)
    public static final class Events {
        @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
            if (event.getEntity() instanceof ServerPlayer p && p.containerMenu instanceof EnchantmentMenu menu && p.tickCount % 10 == 0)
                PacketDistributor.sendToPlayer(p, new ShelfState(menu.containerId, count(menu)));
        }
    }
}

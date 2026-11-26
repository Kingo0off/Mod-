package ru.atl.skills;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(AtlSkills.ID)
public final class AtlSkills {
    public static final String ID = "atl_skills";
    private static final DeferredRegister<AttachmentType<?>> DATA = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ID);
    private static final Map<Rules.Skill, Supplier<AttachmentType<Integer>>> XP = new EnumMap<>(Rules.Skill.class);
    private static final Supplier<AttachmentType<Integer>> BALANCE_VERSION = DATA.register("balance_version",
        () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("version")).copyOnDeath().build());
    static {
        for (Rules.Skill skill : Rules.Skill.values()) {
            XP.put(skill, DATA.register(skill.suffix + "_xp", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("xp")).copyOnDeath().sync((holder, to) -> holder == to, ByteBufCodecs.VAR_INT).build()));
        }
    }
    public AtlSkills(IEventBus bus) { DATA.register(bus); NeoForge.EVENT_BUS.register(this); }
    public static Integer skillXp(Player player, Rules.Skill skill) {
        return player.getExistingDataOrNull(XP.get(skill));
    }
    public static Rules.Requirement requirement(ItemStack stack) {
        return stack.isEmpty() ? null : Rules.requirement(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }
    public static boolean allowed(Player player, ItemStack stack, boolean notify) {
        if (!(player instanceof ServerPlayer sp) || player.isCreative() || player.isSpectator()) return true;
        Rules.Requirement req = requirement(stack);
        if (req == null || Rules.level(req.skill(), sp.getData(XP.get(req.skill()))) >= req.level()) return true;
        if (notify) sp.sendSystemMessage(Component.literal("Нужен уровень «" + req.skill().label + "» " + req.level() + ". Посмотреть навыки: /skills"));
        return false;
    }
    private static void gain(ServerPlayer player, Rules.Skill skill, int amount) {
        if (player.isCreative() || player.isSpectator()) return;
        var key = XP.get(skill); int oldXp = player.getData(key);
        int next = (int)Math.min(Rules.threshold(skill, 50), (long)oldXp + amount); player.setData(key, next);

    }
    @SubscribeEvent public void blockBreak(BreakBlockEvent event) {
        if (event.getPlayer() instanceof ServerPlayer p && !allowed(p, p.getMainHandItem(), true)) {
            event.setCanceled(true); event.setNotifyClient(true);
        }
    }
    @SubscribeEvent public void blockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer p) || event.isCanceled()) return;
        Rules.Requirement r = requirement(event.getTool());
        if (r == null || !allowed(p, event.getTool(), false)) return;
        boolean suitable = switch (r.skill()) {
            case PICKAXE -> true;
            case AXE -> event.getState().is(BlockTags.MINEABLE_WITH_AXE);
            case SHOVEL -> event.getState().is(BlockTags.MINEABLE_WITH_SHOVEL);
            case HOE -> event.getState().is(BlockTags.MINEABLE_WITH_HOE);
            default -> false;
        };
        if (suitable) {
            int amount = r.skill() == Rules.Skill.PICKAXE
                    ? Rules.pickaxeXp(BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).toString()) : 1;
            gain(p, r.skill(), amount);
        }
    }
    @SubscribeEvent public void attack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && !allowed(p, p.getMainHandItem(), true)) event.setCanceled(true);
    }
    @SubscribeEvent public void mobDefeated(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof Mob mob)
                || !(event.getSource().getEntity() instanceof ServerPlayer p)
                || event.getSource().getDirectEntity() != p) return;
        Rules.Requirement r = requirement(p.getMainHandItem());
        if (r != null && (r.skill() == Rules.Skill.SWORD || r.skill() == Rules.Skill.AXE)
                && allowed(p, p.getMainHandItem(), false)) {
            gain(p, r.skill(), Rules.mobXp(mob.getMaxHealth()));
        }
    }
    @SubscribeEvent public void useBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer p && !allowed(p, event.getItemStack(), true)) event.setCanceled(true);
    }
    @SubscribeEvent public void useItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer p && !allowed(p, event.getItemStack(), true)) event.setCanceled(true);
    }
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            if (p.getData(BALANCE_VERSION) == 0) {
                for (Rules.Skill skill : Rules.Skill.values()) {
                    var key = XP.get(skill);
                    p.setData(key, Rules.migrate(skill, p.getData(key)));
                }
                p.setData(BALANCE_VERSION, 1);
            }
            for (Rules.Skill skill : Rules.Skill.values()) { p.getData(XP.get(skill)); p.syncData(XP.get(skill)); }
        }
    }
    @SubscribeEvent public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("skills").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            p.sendSystemMessage(Component.literal("Навыки ATL — красивое меню открывается клавишей K:"));
            for (Rules.Skill skill : Rules.Skill.values()) {
                int xp = p.getData(XP.get(skill)); int level = Rules.level(skill, xp);
                String progress = level == 50 ? "максимум" : (xp - Rules.threshold(skill, level)) + "/" + (Rules.threshold(skill, level + 1) - Rules.threshold(skill, level));
                p.sendSystemMessage(Component.literal(skill.label + ": " + level + " (" + progress + ")"));
            }
            return 1;
        }));
    }
}

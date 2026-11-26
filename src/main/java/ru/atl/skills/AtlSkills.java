package ru.atl.skills;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
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
    static {
        for (Rules.Skill skill : Rules.Skill.values()) {
            XP.put(skill, DATA.register(skill.suffix + "_xp", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("xp")).copyOnDeath().build()));
        }
    }
    public AtlSkills(IEventBus bus) { DATA.register(bus); NeoForge.EVENT_BUS.register(this); }
    private static Rules.Requirement requirement(ItemStack stack) {
        return stack.isEmpty() ? null : Rules.requirement(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }
    public static boolean allowed(Player player, ItemStack stack, boolean notify) {
        if (!(player instanceof ServerPlayer sp) || player.isCreative() || player.isSpectator()) return true;
        Rules.Requirement req = requirement(stack);
        if (req == null || Rules.level(sp.getData(XP.get(req.skill()))) >= req.level()) return true;
        if (notify) sp.sendSystemMessage(Component.literal("Нужен уровень «" + req.skill().label + "» " + req.level() + ". Посмотреть навыки: /skills"));
        return false;
    }
    private static void gain(ServerPlayer player, Rules.Skill skill, int amount) {
        if (player.isCreative() || player.isSpectator()) return;
        var key = XP.get(skill); int oldXp = player.getData(key); int oldLevel = Rules.level(oldXp);
        int next = Math.min(Rules.threshold(50), oldXp + amount); player.setData(key, next);
        int newLevel = Rules.level(next);
        if (newLevel > oldLevel) player.sendSystemMessage(Component.literal("Навык «" + skill.label + "»: уровень " + newLevel + "!"));
    }
    @SubscribeEvent public void blockBreak(BreakBlockEvent event) {
        if (event.getPlayer() instanceof ServerPlayer p && !allowed(p, p.getMainHandItem(), true)) {
            event.setCanceled(true); event.setNotifyClient(true);
        }
    }
    @SubscribeEvent public void blockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer p) || event.isCanceled()) return;
        Rules.Requirement r = requirement(event.getTool());
        if (r != null && r.skill() != Rules.Skill.SWORD && allowed(p, event.getTool(), false)) gain(p, r.skill(), 10);
    }
    @SubscribeEvent public void attack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && !allowed(p, p.getMainHandItem(), true)) event.setCanceled(true);
    }
    @SubscribeEvent public void hit(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer p) || !(event.getEntity() instanceof Mob) || event.getHealthDamage() <= 0) return;
        Rules.Requirement r = requirement(p.getMainHandItem());
        if (r != null && (r.skill() == Rules.Skill.SWORD || r.skill() == Rules.Skill.AXE) && allowed(p, p.getMainHandItem(), false)) gain(p, r.skill(), 10);
    }
    @SubscribeEvent public void useBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer p && !allowed(p, event.getItemStack(), true)) event.setCanceled(true);
    }
    @SubscribeEvent public void useItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer p && !allowed(p, event.getItemStack(), true)) event.setCanceled(true);
    }
    @SubscribeEvent public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("skills").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            p.sendSystemMessage(Component.literal("Навыки ATL:"));
            for (Rules.Skill skill : Rules.Skill.values()) {
                int xp = p.getData(XP.get(skill)); int level = Rules.level(xp);
                String progress = level == 50 ? "максимум" : (xp - Rules.threshold(level)) + "/" + (Rules.threshold(level + 1) - Rules.threshold(level));
                p.sendSystemMessage(Component.literal(skill.label + ": " + level + " (" + progress + ")"));
            }
            return 1;
        }));
    }
}

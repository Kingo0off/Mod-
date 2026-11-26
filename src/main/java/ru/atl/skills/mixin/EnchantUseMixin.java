package ru.atl.skills.mixin;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.atl.skills.EnchantProgress;

@Mixin(ItemStack.class)
public abstract class EnchantUseMixin {
    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private void atl$onUse(int amount, ServerLevel level, LivingEntity owner, Consumer<ItemStack> onBreak, CallbackInfo ci) {
        if (amount > 0 && owner instanceof ServerPlayer player && !player.isCreative() && !player.isSpectator())
            EnchantProgress.use((ItemStack)(Object)this);
    }
}

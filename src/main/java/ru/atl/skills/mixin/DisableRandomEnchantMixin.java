package ru.atl.skills.mixin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EnchantmentMenu.class)
public abstract class DisableRandomEnchantMixin {
    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void atl$disableRandomChoices(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}

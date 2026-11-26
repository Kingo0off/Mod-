package ru.atl.skills.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.atl.skills.AtlSkills;

@Mixin(AbstractContainerMenu.class)
public abstract class CraftGuardMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void atl$guardCraft(int slot, int button, ContainerInput action, Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer)) return;
        AbstractContainerMenu menu = (AbstractContainerMenu)(Object)this;
        boolean result = ((menu instanceof CraftingMenu || menu instanceof InventoryMenu) && slot == 0)
                || (menu instanceof SmithingMenu && slot == 3);
        if (result && slot < menu.slots.size() && !AtlSkills.allowed(player, menu.getSlot(slot).getItem(), true)) {
            ci.cancel(); menu.broadcastFullState();
        }
    }
}

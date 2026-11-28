package ru.atl.skills.mixin;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(EnchantmentMenu.class)
public interface EnchantMenuAccess {
    @Accessor("access") ContainerLevelAccess atl$getAccess();
}

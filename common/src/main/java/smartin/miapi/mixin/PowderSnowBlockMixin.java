package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.CanWalkOnSnow;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin {

    @ModifyReturnValue(method = "canWalkOnPowderSnow(Lnet/minecraft/entity/Entity;)Z", at = @At("RETURN"))
    private static boolean miapi$bypassSnowWalk(boolean original, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack boots = livingEntity.getEquippedStack(EquipmentSlot.FEET);
            if (boots.getItem() instanceof ModularItem) {
                return CanWalkOnSnow.canSnowWalk(boots);
            }
        }
        return original;
    }
}

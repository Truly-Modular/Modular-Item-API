package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.armor.CanWalkOnSnow;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin {

    @ModifyReturnValue(method = "canEntityWalkOnPowderSnow", at = @At("RETURN"))
    private static boolean miapi$bypassSnowWalk(boolean original, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack boots = livingEntity.getItemBySlot(EquipmentSlot.FEET);
            if (ModularItem.isModularItem(boots)) {
                return CanWalkOnSnow.canSnowWalk(boots);
            }
        }
        return original;
    }
}

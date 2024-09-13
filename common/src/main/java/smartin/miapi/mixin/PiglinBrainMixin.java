package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.armor.IsPiglinGold;

@Mixin(PiglinAi.class)
public abstract class PiglinBrainMixin {

    @ModifyReturnValue(method = "isWearingGold", at = @At("RETURN"))
    private static boolean miapi$isGoldItemBypass(boolean original, LivingEntity entity) {
        for (ItemStack armorItem : entity.getArmorSlots()) {
            if (ModularItem.isModularItem(armorItem) && IsPiglinGold.isPiglinGoldItem(armorItem)) {
                //IsPiglinGold.isPiglinGoldItem(armorItem)
                return true;
            }
        }
        return original;
    }
}

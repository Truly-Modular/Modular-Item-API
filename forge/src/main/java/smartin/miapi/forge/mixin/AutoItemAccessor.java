package smartin.miapi.forge.mixin;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(harmonised.pmmo.features.autovalues.AutoItem.class)
public interface AutoItemAccessor {
    @Invoker
    static Map<String, Integer> callGetUtensilData(AutoValueConfig.UtensilTypes utensil, ReqType type, ItemStack stack, boolean asWeapon) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static Map<String, Integer> callGetWearableData(ReqType type, ItemStack stack, boolean isArmor) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static double callGetDamage(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static double callGetAttackSpeed(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static double callGetTier(ToolItem stack) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static double callGetDurability(ItemStack stack) {
        throw new UnsupportedOperationException();
    }
}

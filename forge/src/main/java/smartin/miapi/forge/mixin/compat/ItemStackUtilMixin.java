package smartin.miapi.forge.mixin.compat;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

@Mixin(ItemStackUtils.class)
public class ItemStackUtilMixin {
    @Inject(
            at = @At("HEAD"),
            method = "getMiningLevel(Lnet/minecraft/item/ItemStack;Lcom/minecolonies/api/util/constant/IToolType;)I",
            remap = true,
            cancellable = true,
            require = -1
    )
    private static void miapi$adjustMiningLevel(ItemStack stack, IToolType toolType, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof ModularItem && toolType != null && false) {
            int level = MiningLevelProperty.getMiningLevel(toolType.getName(), stack);
            if (level == 0) {
                level = MiningLevelProperty.isSuitable(stack, toolType.getName()) ? 0 : -1;

            }
            cir.setReturnValue(level);
        }
    }
}

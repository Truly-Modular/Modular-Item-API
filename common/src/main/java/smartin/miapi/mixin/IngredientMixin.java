package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.item.modular.ModularItem;

@Mixin(Ingredient.class)
public class IngredientMixin {
    @Shadow
    private Ingredient.Value[] values;

    @ModifyReturnValue(method = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    public boolean miapi$adjustFakeEnchants(boolean original, ItemStack stack) {
        if (!original && stack != null && ModularItem.isModularItem(stack)) {
            for (Ingredient.Value value : values) {
                if (value instanceof Ingredient.TagValue tagValue) {
                    if (stack.is(tagValue.tag())) {
                        return true;
                    }
                }
            }
        }
        return original;
    }
}

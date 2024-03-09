package smartin.miapi.forge.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.AttributeProperty;

@Mixin(value = ItemStack.class, priority = 100)
public abstract class ItemStackMixinHighPriority {
    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), require = 1, cancellable = true)
    public void miapi$modifyAttributeModifiers(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(AttributeProperty.sortMultimap(cir.getReturnValue()));
        }
    }
}

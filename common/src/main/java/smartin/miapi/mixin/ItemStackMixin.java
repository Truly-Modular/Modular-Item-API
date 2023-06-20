package smartin.miapi.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.MiningLevelProperty;

import static smartin.miapi.modules.properties.AttributeProperty.getAttributeModifiersForSlot;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    @Final
    @Deprecated
    private Item item;

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    public void modifyAttributeModifiers(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        Multimap<EntityAttribute, EntityAttributeModifier> original = cir.getReturnValue();
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(getAttributeModifiersForSlot(stack, slot, ArrayListMultimap.create()));
        }
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    public void modifyDurability(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(ModularItem.getDurability(stack));
        }
    }

    @Inject(method = "isSuitableFor(Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    public void injectIsSuitable(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(MiningLevelProperty.IsSuitable(stack, state));
        }
    }
}

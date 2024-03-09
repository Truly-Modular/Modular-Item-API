package smartin.miapi.forge.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;

import static smartin.miapi.events.MiapiEvents.ITEM_STACK_ATTRIBUTE_EVENT;

/**
 * just please give proper access to modifiable multimaps and shit
 */
@Mixin(ItemAttributeModifierEvent.class)
public class ItemAttributeModifierEventMixin {


    @Shadow
    private Multimap<EntityAttribute, EntityAttributeModifier> unmodifiableModifiers;

    @Inject(
            at = @At("TAIL"),
            method = "<init>(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Lcom/google/common/collect/Multimap;)V",
            remap = true,
            require = -1
    )
    public void miapi$forge_is_incapable_of_making_functional_events(ItemStack stack, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> modifiers, CallbackInfo ci) {
        if (stack.getItem() instanceof ModularItem) {
            Multimap<EntityAttribute, EntityAttributeModifier> attributes = ArrayListMultimap.create();
            attributes.putAll(modifiers);
            ITEM_STACK_ATTRIBUTE_EVENT.invoker().adjust(new MiapiEvents.ItemStackAttributeEventHolder(stack, slot, attributes));
            ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(attributes);
            unmodifiableModifiers = builder.build();
            ItemAttributeModifierEvent event = (ItemAttributeModifierEvent) (Object) this;
            ((ItemAttributeModifierEventAccessor) event).setOriginalModifiers(unmodifiableModifiers);
        }
    }
}

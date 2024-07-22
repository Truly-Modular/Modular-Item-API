package smartin.miapi.fabric.mixin;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.item.modular.PlatformModularItemMethods;

/**
 * This class overwrites the FabricItem methods for Modular items.
 */
@Mixin(PlatformModularItemMethods.class)
public interface PlatformModularItemMethodsMixin extends FabricItem {

    /*
    default Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers = ArrayListMultimap.create();
        ITEM_STACK_ATTRIBUTE_EVENT.invoker().adjust(new MiapiEvents.ItemStackAttributeEventHolder(stack, slot, attributeModifiers));
        return attributeModifiers;
    }

    default boolean isSuitableFor(ItemStack stack, BlockState state) {
        return MiningLevelProperty.isSuitable(stack, state);
    }

     */
}

package smartin.miapi.fabric.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import static smartin.miapi.events.MiapiEvents.ITEM_STACK_ATTRIBUTE_EVENT;

/**
 * This class overwrites the FabricItem methods for Modular items.
 */
@Mixin(PlatformModularItemMethods.class)
public interface PlatformModularItemMethodsMixin extends FabricItem {

    default Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers = ArrayListMultimap.create();
        ITEM_STACK_ATTRIBUTE_EVENT.invoker().adjust(new MiapiEvents.ItemStackAttributeEventHolder(stack, slot, attributeModifiers));
        return attributeModifiers;
    }

    default boolean isSuitableFor(ItemStack stack, BlockState state) {
        return MiningLevelProperty.isSuitable(stack, state);
    }
}

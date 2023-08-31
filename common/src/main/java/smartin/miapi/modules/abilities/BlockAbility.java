package smartin.miapi.modules.abilities;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.abilities.util.EntityAttributeAbility;
import smartin.miapi.modules.properties.BlockProperty;

import java.util.UUID;

/**
 * This Ability is a lesser form of the Block of a Shield.
 * it only blocks a percentage of the Damage, defined by the value of {@link BlockProperty}
 * transforms the Value of {@link BlockProperty} with {@link BlockAbility#calculate(double)} to the actual damage resistance and slowdown percentages
 */
public class BlockAbility extends EntityAttributeAbility {
    UUID attributeUUID = UUID.fromString("3e91990e-4774-11ee-be56-0242ac120002");

    @Override
    protected Multimap<EntityAttribute, EntityAttributeModifier> getAttributes(ItemStack itemStack) {
        Multimap<EntityAttribute, EntityAttributeModifier> multimap = ArrayListMultimap.create();
        double value = BlockProperty.property.getValueSafe(itemStack);
        value = calculate(value);
        multimap.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(attributeUUID,"miapi-block", -value / 100, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        multimap.put(AttributeRegistry.DAMAGE_RESISTANCE, new EntityAttributeModifier(attributeUUID,"miapi-block", value, EntityAttributeModifier.Operation.ADDITION));
        return multimap;
    }

    public static double calculate(double value){
        return (160.0 / (1 + Math.exp(-value / 50.0))) - 80.0;
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
        return true;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 20 * 60 * 60;
    }
}
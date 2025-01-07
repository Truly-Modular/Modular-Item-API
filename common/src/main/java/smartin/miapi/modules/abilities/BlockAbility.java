package smartin.miapi.modules.abilities;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.abilities.util.EntityAttributeAbility;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.properties.AbilityMangerProperty;
import smartin.miapi.modules.properties.BlockPoseProperty;
import smartin.miapi.modules.properties.BlockProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This Ability is a lesser form of the Block of a Shield.
 * it only blocks a percentage of the Damage, defined by the value of {@link BlockProperty}
 * transforms the Value of {@link BlockProperty} with {@link BlockAbility#calculate(double)} to the actual damage resistance and slowdown percentages
 */
public class BlockAbility extends EntityAttributeAbility {
    UUID attributeUUID = UUID.fromString("3e91990e-4774-11ee-be56-0242ac120002");

    public BlockAbility() {
        LoreProperty.bottomLoreSuppliers.add(itemStack -> {
            List<Text> texts = new ArrayList<>();
            if (AbilityMangerProperty.isPrimaryAbility(this, itemStack)) {
                Text raw = Text.translatable("miapi.ability.block.lore");
                texts.add(raw);
            }
            return texts;
        });
    }

    @Override
    protected Multimap<EntityAttribute, EntityAttributeModifier> getAttributes(ItemStack itemStack) {
        Multimap<EntityAttribute, EntityAttributeModifier> multimap = ArrayListMultimap.create();
        double value = BlockProperty.property.getValueSafe(itemStack);
        value = calculate(value);
        multimap.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(attributeUUID, "miapi-block", -(value / 2) / 100, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        multimap.put(AttributeRegistry.DAMAGE_RESISTANCE, new EntityAttributeModifier(attributeUUID, "miapi-block", value, EntityAttributeModifier.Operation.ADDITION));
        return multimap;
    }

    public static double calculate(double value) {
        return (160.0 / (1 + Math.exp(-value / 50.0))) - 80.0;
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return true;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 20 * 60 * 60;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        setAnimation(user, hand);
        return super.use(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        resetAnimation(user);
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        resetAnimation(user);
        super.onStoppedUsingAfter(stack, world, user, remainingUseTicks);
    }

    @Override
    public void onStoppedHolding(ItemStack stack, World world, LivingEntity user) {
        resetAnimation(user);
        super.onStoppedHolding(stack, world, user);
    }

    public void setAnimation(PlayerEntity p, Hand hand) {
        if (p instanceof ServerPlayerEntity player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            ItemStack itemStack = p.getStackInHand(hand);
            if (facet != null) {
                facet.set(BlockPoseProperty.poseProperty.getPoseId(itemStack), player, hand);
            }
        }
    }

    public void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null)
                facet.reset(player);
        }
    }
}
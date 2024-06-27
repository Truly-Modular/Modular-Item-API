package smartin.miapi.modules.abilities;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.abilities.util.EntityAttributeAbility;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.properties.AbilityMangerProperty;
import smartin.miapi.modules.properties.BlockProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * This Ability is a lesser form of the Block of a Shield.
 * it only blocks a percentage of the Damage, defined by the value of {@link BlockProperty}
 * transforms the Value of {@link BlockProperty} with {@link BlockAbility#calculate(double)} to the actual damage resistance and slowdown percentages
 */
public class BlockAbility extends EntityAttributeAbility {
    UUID attributeUUID = UUID.fromString("3e91990e-4774-11ee-be56-0242ac120002");

    public BlockAbility() {
        LoreProperty.bottomLoreSuppliers.add(itemStack -> {
            List<Component> texts = new ArrayList<>();
            if (AbilityMangerProperty.isPrimaryAbility(this, itemStack)) {
                Component raw = Component.translatable("miapi.ability.block.lore");
                texts.add(raw);
            }
            return texts;
        });
    }

    @Override
    protected Multimap<Attribute, AttributeModifier> getAttributes(ItemStack itemStack) {
        Multimap<Attribute, AttributeModifier> multimap = ArrayListMultimap.create();
        double value = BlockProperty.property.getValueSafe(itemStack);
        value = calculate(value);
        multimap.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(attributeUUID, "miapi-block", -(value / 2) / 100, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        multimap.put(AttributeRegistry.DAMAGE_RESISTANCE, new AttributeModifier(attributeUUID, "miapi-block", value, EntityAttributeModifier.Operation.ADDITION));
        return multimap;
    }

    public static double calculate(double value) {
        return (160.0 / (1 + Math.exp(-value / 50.0))) - 80.0;
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return true;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 20 * 60 * 60;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        setAnimation(user, hand);
        return super.use(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        resetAnimation(user);
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        resetAnimation(user);
        super.onStoppedUsingAfter(stack, world, user, remainingUseTicks);
    }

    @Override
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user) {
        resetAnimation(user);
        super.onStoppedHolding(stack, world, user);
    }

    public void setAnimation(Player p, InteractionHand hand) {
        if (p instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null) {
                facet.set("miapi:block", player, hand);
            }
        }
    }

    public void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null)
                facet.reset(player);
        }
    }
}
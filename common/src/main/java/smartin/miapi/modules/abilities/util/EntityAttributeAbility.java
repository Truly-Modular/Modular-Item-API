package smartin.miapi.modules.abilities.util;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * The EntityAttributeAbility class is an abstract implementation of the ItemUseAbility interface.
 * It provides functionality to give the player attributes while holding right-click.
 * Extend this class and implement the getAttributes() method to define the attributes to be applied.
 */
public abstract class EntityAttributeAbility<T> implements ItemUseDefaultCooldownAbility<T>, ItemUseMinHoldAbility<T> {
    Map<LivingEntity, Multimap<Holder<Attribute>, AttributeModifier>> playerEntityMultimapMap = new HashMap<>();

    /**
     * Get the attributes and modifiers to be applied for the specified item stack.
     *
     * @param itemStack The item stack being used.
     * @return The multimap of entity attributes and attribute modifiers.
     */
    protected abstract Multimap<Holder<Attribute>, AttributeModifier> getAttributes(ItemStack itemStack);


    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        user.startUsingItem(hand);
        Multimap<Holder<Attribute>, AttributeModifier> attributeAttributePropertyMultimap = getAttributes(itemStack);
        user.getAttributes().addTransientAttributeModifiers(attributeAttributePropertyMultimap);
        playerEntityMultimapMap.put(user, attributeAttributePropertyMultimap);
        return InteractionResultHolder.consume(itemStack);
    }

    private void remove(ItemStack itemStack, LivingEntity livingEntity) {
        if (livingEntity instanceof Player playerEntity) {
            playerEntity.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        }
        Multimap<Holder<Attribute>, AttributeModifier> map = playerEntityMultimapMap.get(livingEntity);
        if (map != null) {
            livingEntity.getAttributes().removeAttributeModifiers(map);
        }
    }

    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        remove(stack, user);
        return stack;
    }

    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        remove(stack, user);
    }

    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user) {
        remove(stack, user);
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.NONE;
    }
}

package smartin.miapi.modules.abilities.util;

import com.google.common.collect.Multimap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * The EntityAttributeAbility class is an abstract implementation of the ItemUseAbility interface.
 * It provides functionality to give the player attributes while holding right-click.
 * Extend this class and implement the getAttributes() method to define the attributes to be applied.
 */
public abstract class EntityAttributeAbility implements ItemUseAbility {
    Map<LivingEntity, Multimap<EntityAttribute, EntityAttributeModifier>> playerEntityMultimapMap = new HashMap<>();

    /**
     * Get the attributes and modifiers to be applied for the specified item stack.
     *
     * @param itemStack The item stack being used.
     * @return The multimap of entity attributes and attribute modifiers.
     */
    protected abstract Multimap<EntityAttribute, EntityAttributeModifier> getAttributes(ItemStack itemStack);


    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        Multimap<EntityAttribute, EntityAttributeModifier> attributeAttributePropertyMultimap = getAttributes(itemStack);
        attributeAttributePropertyMultimap.forEach((attribute, attributeModifier) -> {
        });
        user.getAttributes().addTemporaryModifiers(attributeAttributePropertyMultimap);
        playerEntityMultimapMap.put(user, attributeAttributePropertyMultimap);
        return TypedActionResult.consume(itemStack);
    }

    private void remove(ItemStack itemStack, LivingEntity livingEntity) {
        if(livingEntity instanceof PlayerEntity playerEntity){
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(itemStack.getItem()));
        }
        Multimap<EntityAttribute, EntityAttributeModifier> map = playerEntityMultimapMap.get(livingEntity);
        if (map != null) {
            livingEntity.getAttributes().removeModifiers(map);
        }
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        remove(stack, user);
        return stack;
    }

    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        remove(stack, user);
    }

    public void onStoppedHolding(ItemStack stack, World world, LivingEntity user) {
        remove(stack, user);
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        return UseAction.NONE;
    }
}

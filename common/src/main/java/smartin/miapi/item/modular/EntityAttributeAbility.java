package smartin.miapi.item.modular;

import com.google.common.collect.Multimap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public abstract class EntityAttributeAbility implements ItemUseAbility {
    Map<LivingEntity, Multimap<EntityAttribute, EntityAttributeModifier>> playerEntityMultimapMap = new HashMap<>();

    protected abstract Multimap<EntityAttribute, EntityAttributeModifier> getAttributes(ItemStack itemStack);

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        Multimap<EntityAttribute, EntityAttributeModifier> attributeAttributePropertyMultimap = getAttributes(itemStack);
        user.getAttributes().addTemporaryModifiers(attributeAttributePropertyMultimap);
        playerEntityMultimapMap.put(user, attributeAttributePropertyMultimap);
        return TypedActionResult.consume(itemStack);
    }

    private void remove(ItemStack itemStack, LivingEntity livingEntity) {
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

    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand){
        return ActionResult.PASS;
    }

    public ActionResult useOnBlock(ItemUsageContext context){
        return ActionResult.PASS;
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        return UseAction.NONE;
    }

    @Override
    public UseAction onUse(World world, PlayerEntity user, Hand hand) {
        return UseAction.NONE;
    }
}

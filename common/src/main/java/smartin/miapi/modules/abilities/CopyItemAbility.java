package smartin.miapi.modules.abilities;

import com.google.gson.JsonObject;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.*;
import net.minecraft.world.World;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;

/**
 * @header Copy Item Ability
 * @description_start This ability aims to be able to copy any other items right click ability.
 * This might not work with some items/mods, as if they check for the executing item this will fail
 * @desciption_end
 * @path /data_types/abilities/copy_item
 * @data id:the id of the item to copy from
 */
public class CopyItemAbility implements ItemUseAbility<CopyItemAbility.ItemContext> {

    @Override
    public boolean allowedOnItem(ItemStack stack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            return true;
        }
        return false;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            return itemContext.item.getUseAction(stack);
        }
        return UseAction.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            return itemContext.item.getMaxUseTime(stack);
        }
        return 0;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            return itemContext.item.use(world, user, hand);
        }
        return TypedActionResult.pass(stack);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            return itemContext.item.finishUsing(stack, world, user);
        }
        return ItemUseAbility.super.finishUsing(stack, world, user);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            itemContext.item.onStoppedUsing(stack, world, user, remainingUseTicks);
            return;
        }
        ItemUseAbility.super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public void onStoppedHolding(ItemStack stack, World world, LivingEntity user) {
        ItemUseAbility.super.onStoppedHolding(stack, world, user);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            return itemContext.item.useOnEntity(stack, user, entity, hand);
        }
        return ItemUseAbility.super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            return itemContext.item.isUsedOnRelease(stack);
        }
        return ItemUseAbility.super.isUsedOnRelease(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            return itemContext.item.useOnBlock(context);
        }
        return ItemUseAbility.super.useOnBlock(context);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        ItemContext itemContext = getSpecialContext(stack, null);
        if (itemContext != null && itemContext.item != null) {
            itemContext.item.usageTick(world, user, stack, remainingUseTicks);
            return;
        }
        ItemUseAbility.super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public ItemContext fromJson(JsonObject jsonObject) {
        if (jsonObject.has("id")) {
            Identifier id = Identifier.tryParse(jsonObject.get("id").getAsString());
            ItemContext itemContext = new ItemContext();
            if (itemContext.item == null && id != null) {
                itemContext.item = Registries.ITEM.get(id);
            }
            return itemContext;
        }
        return ItemUseAbility.super.fromJson(jsonObject);
    }

    public static class ItemContext {
        public Identifier id = Identifier.of("miapi", "empty");
        @AutoCodec.Ignored
        public Item item;
    }
}

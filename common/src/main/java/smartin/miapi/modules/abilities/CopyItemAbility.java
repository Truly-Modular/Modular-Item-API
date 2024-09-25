package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;

/**
 * @header Copy Item Ability
 * @description_start
 * This ability aims to be able to copy any other items right click ability.
 * This might not work with some items/mods, as if they check for the executing item this will fail
 * @desciption_end
 * @path /data_types/abilities/copy_item
 * @data id:the id of the item to copy from
 */
public class CopyItemAbility implements ItemUseAbility<CopyItemAbility.ItemContext> {

    @Override
    public boolean allowedOnItem(ItemStack stack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        if (getSpecialContext(stack).item != null) {
            return true;
        }
        return false;
    }

    @Override
    public UseAnim getUseAction(ItemStack stack) {
        if (getSpecialContext(stack).item != null) {
            return getSpecialContext(stack).item.getUseAnimation(stack);
        }
        return null;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity entity) {
        if (getSpecialContext(stack).item != null) {
            return getSpecialContext(stack).item.getUseDuration(stack, entity);
        }
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (getSpecialContext(stack).item != null) {
            return getSpecialContext(stack).item.use(world, user, hand);
        }
        return null;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        if (getSpecialContext(stack).item != null) {
            return getSpecialContext(stack).item.finishUsingItem(stack, world, user);
        }
        return ItemUseAbility.super.finishUsing(stack, world, user);
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        if (getSpecialContext(stack).item != null) {
            return getSpecialContext(stack).item.useOnRelease(stack);
        }
        return ItemUseAbility.super.useOnRelease(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        ItemUseAbility.super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user) {
        ItemUseAbility.super.onStoppedHolding(stack, world, user);
    }

    @Override
    public InteractionResult useOnEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        return ItemUseAbility.super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (getSpecialContext(stack).item != null) {
            return getSpecialContext(stack).item.useOn(context);
        }
        return ItemUseAbility.super.useOnBlock(context);
    }

    @Override
    public void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (getSpecialContext(stack).item != null) {
            getSpecialContext(stack).item.onUseTick(world, user, stack, remainingUseTicks);
            return;
        }
        ItemUseAbility.super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public ItemContext initialize(ItemContext data, ModuleInstance moduleInstance) {
        ItemContext itemContext = new ItemContext();
        itemContext.id = data.id;
        itemContext.item = BuiltInRegistries.ITEM.get(data.id);
        return itemContext;
    }

    @Override
    public ItemContext getDefaultContext() {
        return new ItemContext();
    }

    @Override
    public <K> ItemContext decode(DynamicOps<K> ops, K prefix) {
        Codec<ItemContext> codec = AutoCodec.of(ItemContext.class).codec();
        return codec.decode(ops, prefix).getOrThrow().getFirst();
    }

    public static class ItemContext {
        public ResourceLocation id = Miapi.id("empty");
        @AutoCodec.Ignored
        public Item item;
    }
}

package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
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
import smartin.miapi.MixinContextFlags;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.function.Supplier;

public class CopyItemAbility implements ItemUseDefaultCooldownAbility<CopyItemAbility.ItemContext>,
        ItemUseMinHoldAbility<CopyItemAbility.ItemContext> {
    public static CopyItemAbility ability;
    public static String KEY = "copy_item";

    public CopyItemAbility() {
        ability = this;
    }

    /**
     * Runs a lambda with the MixinContextFlag temporarily set for this stack/item.
     */
    public static <T> T withFlag(ItemStack stack, CopyItemAbility.ItemContext item, Supplier<T> action) {
        if (item == null) return null;
        try {
            if (item.fakeItemIdentity) {
                MixinContextFlags.IGNORE_NEXT_GET_ITEM_CALL.get().put(stack, item.item);
            }
            return action.get();
        } finally {
            if (item.fakeItemIdentity) {
                MixinContextFlags.IGNORE_NEXT_GET_ITEM_CALL.get().remove(stack);
            }
        }
    }

    public static void withFlag(ItemStack stack, CopyItemAbility.ItemContext item, Runnable action) {
        if (item == null) return;
        try {
            if (item.fakeItemIdentity) {
                MixinContextFlags.IGNORE_NEXT_GET_ITEM_CALL.get().put(stack, item.item);
            }
            action.run();
        } finally {
            if (item.fakeItemIdentity) {
                MixinContextFlags.IGNORE_NEXT_GET_ITEM_CALL.get().remove(stack);
            }
        }
    }

    @Override
    public boolean allowedOnItem(ItemStack stack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, ItemContext context) {
        context.initialize();
        return context.item != null;
    }

    @Override
    public UseAnim getUseAction(ItemStack stack, ItemContext context) {
        return withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.getUseAnimation(stack));
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity entity, ItemContext context) {
        return withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.getUseDuration(stack, entity));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, ItemContext context) {
        ItemStack stack = user.getItemInHand(hand);
        return withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.use(world, user, hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user, ItemContext context) {
        ItemStack result = withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.finishUsingItem(stack, world, user));
        if (!world.isClientSide && user instanceof Player player) {
            player.getCooldowns().addCooldown(stack.getItem(), (int) context.cooldown.getValue());
        }
        return result;
    }

    @Override
    public boolean useOnRelease(ItemStack stack, ItemContext context) {
        Boolean result = withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.useOnRelease(stack));
        return result != null ? result : ItemUseMinHoldAbility.super.useOnRelease(stack, context);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, ItemContext context) {
        withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.releaseUsing(stack, world, user, remainingUseTicks));
    }

    @Override
    public int getCooldown(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).cooldown.getValue();
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).minHold.getValue();
    }


    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, ItemContext context) {
        if (!(user instanceof Player player)) return;

        int used = getMaxUseTime(stack, user, context) - remainingUseTicks;
        if (used < context.minHold.getValue()) {
            // Released too early → do nothing
            return;
        }

        // Normal finishUsing behavior (proxy)
        withFlag(stack, context, () -> {
            Item result = context.item;
            result.finishUsingItem(stack, world, user);
        });

        // Apply cooldown
        if (!world.isClientSide) {
            player.getCooldowns().addCooldown(stack.getItem(), (int) context.cooldown.getValue());
        }
    }


    @Override
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user, ItemContext context) {
        withFlag(stack, getSpecialContext(stack),
                () -> ItemUseMinHoldAbility.super.onStoppedHolding(stack, world, user, context));
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, ItemContext abilityContext) {
        InteractionResult result = withFlag(context.getItemInHand(), getSpecialContext(context.getItemInHand()),
                () -> getSpecialContext(context.getItemInHand()).item.useOn(context));
        if (!context.getLevel().isClientSide && result.indicateItemUse() && context.getPlayer() != null) {
            context.getPlayer().getCooldowns().addCooldown(context.getItemInHand().getItem(), (int) abilityContext.cooldown.getValue());
        }
        return result;
    }

    @Override
    public void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks, ItemContext context) {
        if (getSpecialContext(stack).item != null) {
            withFlag(stack, getSpecialContext(stack),
                    () -> getSpecialContext(stack).item.onUseTick(world, user, stack, remainingUseTicks));
        } else {
            ItemUseMinHoldAbility.super.usageTick(world, user, stack, remainingUseTicks, context);
        }
    }

    @Override
    public Codec<ItemContext> getCodec() {
        return AutoCodec.of(ItemContext.class).codec();
    }

    @Override
    public ItemContext initialize(ItemContext data, ModuleInstance moduleInstance) {
        ItemContext itemContext = new ItemContext();
        itemContext.id = data.id;
        itemContext.item = BuiltInRegistries.ITEM.get(data.id);
        itemContext.fakeItemIdentity = data.fakeItemIdentity;

        itemContext.minHold = data.minHold.initialize(moduleInstance);
        itemContext.cooldown = data.cooldown.initialize(moduleInstance);

        return itemContext;
    }


    @Override
    public ItemContext getDefaultContext() {
        return new ItemContext();
    }

    public static class ItemContext {
        public ResourceLocation id = Miapi.id("empty");
        @AutoCodec.Ignored
        public Item item;
        @CodecBehavior.Optional
        @AutoCodec.Name("fake_item_identity")
        public boolean fakeItemIdentity = false;
        @CodecBehavior.Optional
        @AutoCodec.Name("min_hold")
        public DoubleOperationResolvable minHold = new DoubleOperationResolvable(0);

        @CodecBehavior.Optional
        public DoubleOperationResolvable cooldown = new DoubleOperationResolvable(20);


        public void initialize() {
            item = BuiltInRegistries.ITEM.get(id);
        }

        public ItemContext() {
        }

        public ItemContext(Item item) {
            this.item = item;
            this.id = item.arch$registryName();
        }
    }
}

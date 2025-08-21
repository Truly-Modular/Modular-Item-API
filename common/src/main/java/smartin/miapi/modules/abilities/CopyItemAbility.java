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
import smartin.miapi.modules.abilities.util.ItemUseAbility;

import java.util.function.Supplier;

public class CopyItemAbility implements ItemUseAbility<CopyItemAbility.ItemContext> {
    public static CopyItemAbility ability;
    public static String KEY = "copy_item";

    public CopyItemAbility() {
        ability = this;
    }

    /**
     * Runs a lambda with the MixinContextFlag temporarily set for this stack/item.
     */
    private static <T> T withFlag(ItemStack stack, CopyItemAbility.ItemContext item, Supplier<T> action) {
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

    private static void withFlag(ItemStack stack, CopyItemAbility.ItemContext item, Runnable action) {
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
    public boolean allowedOnItem(ItemStack stack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        ItemContext context = getSpecialContext(stack);
        context.initialize();
        return context.item != null;
    }

    @Override
    public UseAnim getUseAction(ItemStack stack) {
        return withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.getUseAnimation(stack));
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity entity) {
        return withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.getUseDuration(stack, entity));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        return withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.use(world, user, hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        return withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.finishUsingItem(stack, world, user));
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        Boolean result = withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.useOnRelease(stack));
        return result != null ? result : ItemUseAbility.super.useOnRelease(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        withFlag(stack, getSpecialContext(stack),
                () -> getSpecialContext(stack).item.releaseUsing(stack, world, user, remainingUseTicks));
    }

    @Override
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user) {
        withFlag(stack, getSpecialContext(stack),
                () -> ItemUseAbility.super.onStoppedHolding(stack, world, user));
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        return withFlag(context.getItemInHand(), getSpecialContext(context.getItemInHand()),
                () -> getSpecialContext(context.getItemInHand()).item.useOn(context));
    }

    @Override
    public void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (getSpecialContext(stack).item != null) {
            withFlag(stack, getSpecialContext(stack),
                            () ->getSpecialContext(stack).item.onUseTick(world, user, stack, remainingUseTicks));
        } else {
            ItemUseAbility.super.usageTick(world, user, stack, remainingUseTicks);
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

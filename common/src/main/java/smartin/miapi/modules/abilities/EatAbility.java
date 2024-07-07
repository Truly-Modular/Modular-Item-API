package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows items to be eaten}
 */
public class EatAbility implements ItemUseDefaultCooldownAbility<EatAbility.EatRawData>, ItemUseMinHoldAbility<EatAbility.EatRawData> {
    public static final String KEY = "eat";

    public EatAbility() {
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        EatRawData data = getSpecialContext(itemStack);
        return data != null && (data.alwaysEdible || player.getFoodData().needsFood());
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.EAT;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return getSpecialContext(itemStack).eatTicks();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (user.getCooldowns().isOnCooldown(user.getItemInHand(hand).getItem())) {
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }

        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (remainingUseTicks <= 0) {
            user.releaseUsingItem();
            EatRawData context = getSpecialContext(stack);

            boolean isClient = user.level().isClientSide;
            if (isClient) {
                user.level().playLocalSound(user.getX(), user.getY(), user.getZ(), user.getEatingSound(stack), SoundSource.NEUTRAL, 1, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.4f, true);
            }
            if (user instanceof Player player) {
                if (!isClient) {
                    player.getFoodData().eat(context.getNutrition(), (float) context.getSaturation());
                    context.effects.forEach(possibleEffect -> {
                        if (user.level().getRandom().nextFloat() < possibleEffect.probability()) {
                            player.addEffect(new MobEffectInstance(possibleEffect.effect()));
                        }
                    });
                    if (player instanceof ServerPlayer serverPlayerEntity) {
                        if (context.consumeOnEat()) {
                            stack.shrink(1);
                        } else {
                            stack.hurtAndBreak(context.durabilityDamage(), serverPlayerEntity, getEquipmentSlot(user.getUsedItemHand()));
                        }
                    }
                } else {
                    user.level().playLocalSound(user.getX(), user.getY(), user.getZ(), SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 0.5f, world.random.nextFloat() * 0.1f + 0.9f, true);
                }
            }
        }
    }

    @Override
    public <K> EatAbility.EatRawData decode(DynamicOps<K> ops, K prefix) {
        return EatRawData.codec.decode(ops, prefix).getOrThrow().getFirst();
    }

    @Override
    public EatAbility.EatRawData getDefaultContext() {
        return null;
    }

    @Override
    public int getCooldown(ItemStack itemStack) {
        return getSpecialContext(itemStack).getCooldown();
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return getSpecialContext(itemStack).eatTicks();
    }

    public static class EatRawData {
        public static final Codec<EatRawData> codec = AutoCodec.of(EatRawData.class).codec();

        public DoubleOperationResolvable nutrition;
        public DoubleOperationResolvable saturation;
        public DoubleOperationResolvable eat_ticks = new DoubleOperationResolvable(32);
        public DoubleOperationResolvable cooldown = new DoubleOperationResolvable(0);
        public @CodecBehavior.Optional DoubleOperationResolvable durability = new DoubleOperationResolvable(0);
        public @CodecBehavior.Optional boolean alwaysEdible = false;
        @CodecBehavior.Override("effect_codec")
        public static Codec<List<FoodProperties.PossibleEffect>> effect_codec = Codec.list(FoodProperties.PossibleEffect.CODEC);
        public @CodecBehavior.Optional List<FoodProperties.PossibleEffect> effects = new ArrayList<>();

        public EatRawData initialize(ModuleInstance instance) {
            this.nutrition.initialize(instance);
            this.saturation.initialize(instance);
            this.durability.initialize(instance);
            return this;
        }

        public EatRawData merge(EatRawData merge, MergeType mergeType) {
            return merge(this, merge, mergeType);
        }

        public static EatRawData merge(EatRawData left, EatRawData right, MergeType mergeType) {
            EatRawData rawData = new EatRawData();
            rawData.nutrition = DoubleOperationResolvable.merge(left.nutrition, right.nutrition, mergeType);
            rawData.saturation = DoubleOperationResolvable.merge(left.saturation, right.saturation, mergeType);
            rawData.durability = DoubleOperationResolvable.merge(left.durability, right.durability, mergeType);
            if (MergeType.OVERWRITE.equals(mergeType)) {
                rawData.effects = right.effects;
            } else {
                List<FoodProperties.PossibleEffect> mergedEffects = new ArrayList<>(left.effects);
                mergedEffects.addAll(right.effects);
                rawData.effects = mergedEffects;
            }
            rawData.alwaysEdible = left.alwaysEdible || right.alwaysEdible;
            return rawData;
        }

        int getNutrition() {
            return (int) nutrition.evaluate(0.0, 1.0);
        }

        int getCooldown() {
            return (int) cooldown.evaluate(0.0, 0.0);
        }

        double getSaturation() {
            return saturation.evaluate(0.0, 1.0);
        }

        int eatTicks() {
            return (int) eat_ticks.evaluate(0.0, 32);
        }

        int durabilityDamage() {
            return (int) eat_ticks.evaluate(0.0, 0);
        }

        boolean consumeOnEat() {
            return eat_ticks.evaluate(0.0) == null;
        }
    }
}

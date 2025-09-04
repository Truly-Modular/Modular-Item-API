package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import smartin.miapi.modules.properties.util.InitializeAble;
import smartin.miapi.modules.properties.util.MergeAble;
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
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, EatRawData data) {
        return data != null && (data.alwaysEdible || player.getFoodData().needsFood());
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack, EatRawData context) {
        return UseAnim.EAT;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity entity, EatRawData context) {
        return getSpecialContext(itemStack).eatTicks();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, EatRawData context) {
        if (user.getCooldowns().isOnCooldown(user.getItemInHand(hand).getItem())) {
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }

        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks, EatRawData context) {
        if (remainingUseTicks <= 1) {
            user.releaseUsingItem();

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
    public Codec<EatRawData> getCodec() {
        return EatRawData.CODEC;
    }

    @Override
    public EatAbility.EatRawData getDefaultContext() {
        return null;
    }

    @Override
    public EatRawData merge(EatRawData left, EatRawData right, MergeType mergeType) {
        return left.merge(left, right, mergeType);
    }

    @Override
    public EatRawData initialize(EatRawData left, ModuleInstance moduleInstance) {
        return left.initialize(left,moduleInstance);
    }


    @Override
    public int getCooldown(ItemStack itemStack) {
        return getSpecialContext(itemStack).getCooldown();
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return getSpecialContext(itemStack).eatTicks();
    }

    public static class EatRawData implements MergeAble<EatRawData>, InitializeAble<EatRawData> {
        //public static final Codec<EatRawData> codec = AutoCodec.of(EatRawData.class).codec();

        public static final Codec<EatRawData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DoubleOperationResolvable.CODEC.fieldOf("nutrition")
                        .orElse(new DoubleOperationResolvable(0))
                        .forGetter(d -> d.nutrition),
                DoubleOperationResolvable.CODEC.fieldOf("saturation")
                        .orElse(new DoubleOperationResolvable(0))
                        .forGetter(d -> d.saturation),
                DoubleOperationResolvable.CODEC.fieldOf("eat_ticks")
                        .orElse(new DoubleOperationResolvable(32))
                        .forGetter(d -> d.eat_ticks),
                DoubleOperationResolvable.CODEC.fieldOf("cooldown")
                        .orElse(new DoubleOperationResolvable(0))
                        .forGetter(d -> d.cooldown),
                DoubleOperationResolvable.CODEC.optionalFieldOf("durability", new DoubleOperationResolvable(0))
                        .forGetter(d -> d.durability),
                Codec.BOOL.optionalFieldOf("alwaysEdible", false)
                        .forGetter(d -> d.alwaysEdible),
                Codec.list(FoodProperties.PossibleEffect.CODEC).optionalFieldOf("effects", new ArrayList<>())
                        .forGetter(d -> d.effects)
        ).apply(instance, EatRawData::new));

        public DoubleOperationResolvable nutrition;
        public DoubleOperationResolvable saturation;
        public DoubleOperationResolvable eat_ticks;
        public DoubleOperationResolvable cooldown;
        public DoubleOperationResolvable durability;
        public boolean alwaysEdible;
        @CodecBehavior.Override("effect_codec")
        public static Codec<List<FoodProperties.PossibleEffect>> effect_codec = Codec.list(FoodProperties.PossibleEffect.CODEC);
        public @CodecBehavior.Optional List<FoodProperties.PossibleEffect> effects = new ArrayList<>();

        public EatRawData(DoubleOperationResolvable nutrition,
                          DoubleOperationResolvable saturation,
                          DoubleOperationResolvable eat_ticks,
                          DoubleOperationResolvable cooldown,
                          DoubleOperationResolvable durability,
                          boolean alwaysEdible,
                          List<FoodProperties.PossibleEffect> effects) {
            this.nutrition = nutrition;
            this.saturation = saturation;
            this.eat_ticks = eat_ticks;
            this.cooldown = cooldown;
            this.durability = durability;
            this.alwaysEdible = alwaysEdible;
            this.effects = effects;
        }

        public EatRawData() {
        }


        public EatRawData initialize(EatRawData property,ModuleInstance instance) {
            return new EatRawData(
                    property.nutrition.initialize(instance),
                    property.saturation.initialize(instance),
                    property.eat_ticks.initialize(instance),
                    property.cooldown.initialize(instance),
                    property.durability.initialize(instance),
                    property.alwaysEdible,
                    property.effects);
        }


        public EatRawData merge(EatRawData left, EatRawData right, MergeType mergeType) {
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
            return (int) durability.evaluate(0.0, 0);
        }

        boolean consumeOnEat() {
            return durability.evaluate(0.0) == null;
        }
    }
}

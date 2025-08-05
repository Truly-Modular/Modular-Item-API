package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

/**
 * This Ability allows you to use the Trident riptide Effect
 */
//TODO:rework this again
public class RiptideAbility implements ItemUseDefaultCooldownAbility<RiptideAbility.RiptideContextJson>, ItemUseMinHoldAbility<RiptideAbility.RiptideContextJson> {
    public static Codec<RiptideContextJson> CODEC = AutoCodec.of(RiptideContextJson.class).codec();
    public static String KEY = "riptide";

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player) == 0) {
            return false;
        }
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity entity) {
        return 7200;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(itemStack);
        } else if (world instanceof ServerLevel serverLevel && EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, itemStack, user) > 0 && !user.isInWaterOrRain()) {
            return InteractionResultHolder.fail(itemStack);
        } else {
            user.startUsingItem(hand);
            return InteractionResultHolder.consume(itemStack);
        }
    }

    @Override
    public Codec<RiptideContextJson> getCodec() {
        return RiptideAbility.CODEC;
    }

    public void onStoppedUsingAfter(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (livingEntity instanceof Player player) {
            int var6 = this.getMaxUseTime(stack, livingEntity) - timeCharged;
            if (var6 >= 10) {
                float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
                if (player.isInWaterOrRain()) {
                    if (!isTooDamagedToUse(stack)) {
                        Holder<SoundEvent> holder = (Holder) EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                        if (f > 0.0F) {
                            float g = player.getYRot();
                            float h = player.getXRot();
                            float j = -Mth.sin(g * 0.017453292F) * Mth.cos(h * 0.017453292F);
                            float k = -Mth.sin(h * 0.017453292F);
                            float l = Mth.cos(g * 0.017453292F) * Mth.cos(h * 0.017453292F);
                            float m = Mth.sqrt(j * j + k * k + l * l);
                            j *= f / m;
                            k *= f / m;
                            l *= f / m;
                            player.push((double) j, (double) k, (double) l);
                            player.startAutoSpinAttack(20, 8.0F, stack);
                            if (player.onGround()) {
                                float n = 1.1999999F;
                                player.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
                            }

                            level.playSound((Player) null, player, (SoundEvent) holder.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        }

                    }
                }
            }
        }
    }

    private static boolean isTooDamagedToUse(ItemStack stack) {
        return stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @Override
    public RiptideContextJson getDefaultContext() {
        return null;
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).minUse.getValue();
    }


    @Override
    public int getCooldown(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).cooldown.getValue();
    }

    @Override
    public RiptideContextJson initialize(RiptideContextJson json, ModuleInstance moduleInstance) {
        return json.initialize(moduleInstance);
    }

    @Override
    public RiptideContextJson merge(RiptideContextJson left, RiptideContextJson right, MergeType mergeType) {
        RiptideContextJson merged = new RiptideContextJson();
        merged.cooldown = left.cooldown.merge(right.cooldown, mergeType);
        merged.minUse = left.minUse.merge(right.minUse, mergeType);
        merged.spinDuration = left.spinDuration.merge(right.spinDuration, mergeType);
        merged.riptideStrength = left.riptideStrength.merge(right.riptideStrength, mergeType);
        if (MergeType.EXTEND.equals(mergeType) && left.customSound != null) {
            merged.customSound = left.customSound;
        } else {
            merged.customSound = right.customSound;
        }
        return merged;
    }

    public static class RiptideContextJson {
        @CodecBehavior.Optional
        public DoubleOperationResolvable cooldown = new DoubleOperationResolvable(20);
        @AutoCodec.Name("min_use")
        @CodecBehavior.Optional
        public DoubleOperationResolvable minUse = new DoubleOperationResolvable(10);
        @AutoCodec.Name("spin_duration_base")
        @CodecBehavior.Optional
        public DoubleOperationResolvable spinDuration = new DoubleOperationResolvable(20);
        @AutoCodec.Name("riptide_strength")
        @CodecBehavior.Optional
        public DoubleOperationResolvable riptideStrength = new DoubleOperationResolvable(20);
        @CodecBehavior.Optional
        @AutoCodec.Name("custom_sound")
        public ResourceLocation customSound = null;

        public RiptideContextJson() {

        }

        public RiptideContextJson initialize(ModuleInstance moduleInstance) {
            RiptideContextJson init = new RiptideContextJson();
            init.cooldown = cooldown.initialize(moduleInstance);
            init.minUse = minUse.initialize(moduleInstance);
            init.spinDuration = spinDuration.initialize(moduleInstance);
            init.riptideStrength = riptideStrength.initialize(moduleInstance);
            init.customSound = customSound;
            return init;
        }

        public SoundEvent resolveSoundEvent(int riptideLevel) {
            if (customSound != null && BuiltInRegistries.SOUND_EVENT.containsKey(customSound)) {
                return BuiltInRegistries.SOUND_EVENT.get(customSound);
            }

            if (riptideLevel >= 3) {
                return SoundEvents.TRIDENT_RIPTIDE_3.value();
            } else if (riptideLevel == 2) {
                return SoundEvents.TRIDENT_RIPTIDE_2.value();
            }
            return SoundEvents.TRIDENT_RIPTIDE_1.value();
        }
    }
}

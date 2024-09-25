package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
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

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
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

    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof Player playerEntity && world instanceof ServerLevel serverLevel) {
            int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
            if (i >= 10) {
                RiptideContextJson riptideContextJson = getSpecialContext(stack);
                int j = EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, stack, user);

                playerEntity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                float f = playerEntity.getYRot();
                float g = playerEntity.getXRot();
                float h = -Mth.sin(f * 0.017453292F) * Mth.cos(g * 0.017453292F);
                float k = -Mth.sin(g * 0.017453292F);
                float l = Mth.cos(f * 0.017453292F) * Mth.cos(g * 0.017453292F);
                float m = Mth.sqrt(h * h + k * k + l * l);
                float n = (float) (riptideContextJson.riptideStrength.getValue() * ((1.0F + j) / 4.0F));
                h *= n / m;
                k *= n / m;
                l *= n / m;
                playerEntity.push(h, k, l);
                playerEntity.startAutoSpinAttack((int) riptideContextJson.spinDuration.getValue(), EnchantmentHelper.getTridentSpinAttackStrength(stack, user), stack);
                if (playerEntity.onGround()) {
                    playerEntity.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
                }

                SoundEvent soundEvent = riptideContextJson.resolveSoundEvent(j);
                world.playSound(null, playerEntity, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public <K> RiptideContextJson decode(DynamicOps<K> ops, K prefix) {
        return CODEC.decode(ops, prefix).getOrThrow().getFirst();
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
    public RiptideContextJson initialize(RiptideContextJson json, ModuleInstance moduleInstance) {
        return json.initialize(moduleInstance);
    }

    @Override
    public int getCooldown(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).cooldown.getValue();
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
        public DoubleOperationResolvable minUse = new DoubleOperationResolvable(10);
        @AutoCodec.Name("spin_duration_base")
        public DoubleOperationResolvable spinDuration = new DoubleOperationResolvable(20);
        @AutoCodec.Name("riptide_strength")
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

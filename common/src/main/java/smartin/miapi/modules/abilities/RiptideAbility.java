package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
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
    public static String KEY = "riptide";

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, RiptideContextJson context) {
        if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player) == 0) {
            return false;
        }
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack, RiptideContextJson context) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity entity, RiptideContextJson context) {
        return 7200;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, RiptideContextJson context) {
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

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level level, LivingEntity entity, int timeCharged, RiptideContextJson context) {
        if (!(entity instanceof Player player)) {
            return; // Server-side only
        }

        int useTime = this.getMaxUseTime(stack, entity, context);
        int chargeDuration = useTime - timeCharged;

        // Minimum charge threshold (vanilla is 10 ticks)
        if (chargeDuration < 10) {
            return;
        }

        // Base enchant-derived riptide push strength (0, 1.5, 2.0, 2.5)
        float vanillaStrength = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);

        // Final strength = apply DoubleOperationResolvable modifiers
        double evaluatedStrength = context.riptideStrength.evaluate(vanillaStrength).orElse((double)vanillaStrength);
        float finalPushStrength = (float) evaluatedStrength;

        // Can only riptide in water / rain
        if (!player.isInWaterOrRain()) {
            return;
        }

        if (isTooDamagedToUse(stack)) {
            return;
        }

        // Sound selection
        int riptideLevel = (int) vanillaStrength; // vanilla: 1, 2, 3
        SoundEvent sound = context.resolveSoundEvent(riptideLevel);

        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

        // No push if final velocity is zero
        if (finalPushStrength <= 0f) {
            return;
        }

        // ---------------------------------------------------------
        // Compute directional impulse (same as vanilla)
        // ---------------------------------------------------------
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        float xRaw = -Mth.sin(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD);
        float yRaw = -Mth.sin(pitch * Mth.DEG_TO_RAD);
        float zRaw =  Mth.cos(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD);

        float len = Mth.sqrt(xRaw * xRaw + yRaw * yRaw + zRaw * zRaw);
        float x = xRaw * (finalPushStrength / len);
        float y = yRaw * (finalPushStrength / len);
        float z = zRaw * (finalPushStrength / len);

        // ---------------------------------------------------------
        // Apply push + spinning attack (server-side)
        // ---------------------------------------------------------
        player.push(x, y, z);
        //player.move(MoverType.SELF,new Vec3(x * 100, y * 100, z * 100));

        float spinDurationTicks = (float) context.spinDuration.getValue(); // configurably scaled
        player.startAutoSpinAttack((int) spinDurationTicks, 8.0F, stack);

        // Ground jump boost (vanilla)
        if (player.onGround()) {
            player.move(MoverType.SELF, new Vec3(0.0, 1.2, 0.0));
        }

        // Play riptide sound
        level.playSound(null, player, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
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

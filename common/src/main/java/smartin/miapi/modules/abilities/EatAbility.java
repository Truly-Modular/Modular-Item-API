package smartin.miapi.modules.abilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.EdibleProperty;

/**
 * Allows items to be eaten, use in conjunction with {@link EdibleProperty}
 */
public class EatAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    public static final String KEY = "eat";

    public EatAbility() {
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        EdibleProperty.DataHolder data = EdibleProperty.get(itemStack);
        return data != null && (data.alwaysEdible || player.getFoodData().needsFood());
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.EAT;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return (int) (32 * EdibleProperty.get(itemStack).eatingSpeed);
    }

    public int getMaxUseTime(EdibleProperty.DataHolder data) {
        return (int) (32 * data.eatingSpeed);
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
            EdibleProperty.DataHolder data = EdibleProperty.get(stack);

            boolean isClient = user.level().isClientSide;
            if (isClient) {
                user.level().playLocalSound(user.getX(), user.getY(), user.getZ(), user.getEatingSound(stack), SoundSource.NEUTRAL, 1, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.4f, true);
            }
            if (user instanceof Player player) {
                if (!isClient && data != null) {
                    player.getFoodData().eat(data.hunger, (float) data.saturation);
                    data.effects.forEach(e -> player.addEffect(new MobEffectInstance(e)));
                    if (player instanceof ServerPlayer serverPlayerEntity) {
                        data.finishedEat(stack, world.getRandom(), serverPlayerEntity);
                    }
                } else {
                    user.level().playLocalSound(user.getX(), user.getY(), user.getZ(), SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 0.5f, world.random.nextFloat() * 0.1f + 0.9f, true);
                }
            }
        }
    }
}

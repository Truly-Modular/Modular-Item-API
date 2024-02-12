package smartin.miapi.modules.abilities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.EdibleProperty;

/**
 * Allows items to be eaten, use in conjunction with {@link EdibleProperty}
 */
public class EatAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    public static final String KEY = "eat";

    public EatAbility() {}

    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        EdibleProperty.Holder data = EdibleProperty.get(itemStack);
        return data != null && (data.alwaysEdible || player.getHungerManager().isNotFull());
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        return UseAction.EAT;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return (int) (32*EdibleProperty.get(itemStack).eatingSpeed);
    }

    public int getMaxUseTime(EdibleProperty.Holder data) {
        return (int) (32*data.eatingSpeed);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.getItemCooldownManager().isCoolingDown(user.getStackInHand(hand).getItem())) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (remainingUseTicks <= 0) {
            user.stopUsingItem();
            EdibleProperty.Holder data = EdibleProperty.get(stack);

            boolean isClient = user.getWorld().isClient;
            if (isClient) {
                user.getWorld().playSound(user.getX(), user.getY(), user.getZ(), user.getEatSound(stack), SoundCategory.NEUTRAL, 1, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.4f, true);
            }
            if (user instanceof PlayerEntity player) {
                if (!isClient && data != null) {
                    player.getHungerManager().add(data.hunger, (float) data.saturation);
                    data.effects.forEach(e -> player.addStatusEffect(new StatusEffectInstance(e)));
                } else {
                    user.getWorld().playSound(user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.NEUTRAL, 0.5f, world.random.nextFloat() * 0.1f + 0.9f, true);
                }
            }
        }
    }
}

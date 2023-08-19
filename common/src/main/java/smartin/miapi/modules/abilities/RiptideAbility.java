package smartin.miapi.modules.abilities;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.RiptideProperty;

/**
 * This Ability allows you to use the Trident riptide Effect
 */
public class RiptideAbility implements ItemUseAbility {
    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
        RiptideProperty.RiptideJson json = RiptideProperty.getData(itemStack);
        if (json == null) return false;
        boolean missingWater = !player.isTouchingWaterOrRain();
        boolean missingLava = json.allowLava && !player.isInLava();
        if (json.needsWater && (missingWater && missingLava)) {
            return false;
        }
        if (json.needRiptideEnchant && EnchantmentHelper.getRiptide(itemStack) <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 7200;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1) {
            return TypedActionResult.fail(itemStack);
        } else if (EnchantmentHelper.getRiptide(itemStack) > 0 && !user.isTouchingWaterOrRain()) {
            return TypedActionResult.fail(itemStack);
        } else {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
    }

    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                int j = EnchantmentHelper.getRiptide(stack);
                RiptideProperty.RiptideJson json = RiptideProperty.getData(stack);

                playerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                float f = playerEntity.getYaw();
                float g = playerEntity.getPitch();
                float h = -MathHelper.sin(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                float k = -MathHelper.sin(g * 0.017453292F);
                float l = MathHelper.cos(f * 0.017453292F) * MathHelper.cos(g * 0.017453292F);
                float m = MathHelper.sqrt(h * h + k * k + l * l);
                float n = (float) (json.riptideStrength * ((1.0F + j) / 4.0F));
                h *= n / m;
                k *= n / m;
                l *= n / m;
                playerEntity.addVelocity(h, k, l);
                playerEntity.useRiptide(20);
                if (playerEntity.isOnGround()) {
                    playerEntity.move(MovementType.SELF, new Vec3d(0.0, 1.1999999284744263, 0.0));
                }

                SoundEvent soundEvent;
                if (j >= 3) {
                    soundEvent = SoundEvents.ITEM_TRIDENT_RIPTIDE_3;
                } else if (j == 2) {
                    soundEvent = SoundEvents.ITEM_TRIDENT_RIPTIDE_2;
                } else {
                    soundEvent = SoundEvents.ITEM_TRIDENT_RIPTIDE_1;
                }

                world.playSoundFromEntity((PlayerEntity) null, playerEntity, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}

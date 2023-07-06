package smartin.miapi.modules.abilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import smartin.miapi.mixin.LivingEntityAccessor;
import smartin.miapi.modules.abilities.util.AttackUtil;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.HeavyAttackProperty;

/**
 * This Ability allows a stronger attack than the normal left click.
 * Has Configurable range and default sweeping and a scale factor for Damage
 */
public class HeavyAttackAbility implements ItemUseAbility {
    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
        return HeavyAttackProperty.property.hasHeavyAttack(itemStack);
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(user.getItemCooldownManager().isCoolingDown(user.getStackInHand(hand).getItem())){
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }


    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        HeavyAttackProperty.HeavyAttackJson heavyAttackJson = HeavyAttackProperty.property.get(stack);
        double damage = heavyAttackJson.damage;
        double sweeping = heavyAttackJson.sweeping;
        double range = heavyAttackJson.range;
        double minHold = heavyAttackJson.minHold;
        double cooldown = heavyAttackJson.cooldown;

        if (user instanceof PlayerEntity player && getMaxUseTime(stack) - remainingUseTicks > minHold) {
            EntityHitResult entityHitResult = AttackUtil.raycastFromPlayer(range, player);
            if (entityHitResult != null) {
                Entity target2 = entityHitResult.getEntity();
                if (target2 instanceof LivingEntity target) {
                    ((LivingEntityAccessor) player).attacking(target);
                    damage = ((float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * damage);
                    AttackUtil.performAttack(player, target, (float) damage, true);
                    if (sweeping > 0) {
                        AttackUtil.performSweeping(player, target, (float) sweeping, (float) damage);
                    }
                    player.swingHand(player.getActiveHand());
                    player.getItemCooldownManager().set(stack.getItem(), (int) cooldown);
                }
            }
        }
    }
}

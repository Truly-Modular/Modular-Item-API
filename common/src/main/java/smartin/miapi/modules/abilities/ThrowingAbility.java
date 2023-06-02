package smartin.miapi.modules.abilities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import smartin.miapi.Miapi;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectile;
import smartin.miapi.modules.abilities.util.ItemUseAbility;

/**
 * This Ability allows you to throw the Item in question like a Trident
 */
public class ThrowingAbility implements ItemUseAbility {

    public ThrowingAbility(){
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
        return true;
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                playerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                if (!world.isClient) {
                    stack.damage(1, playerEntity, (p) -> {
                        p.sendToolBreakStatus(user.getActiveHand());
                    });
                    ItemProjectile tridentEntity = new ItemProjectile(world, playerEntity, stack);
                    world.spawnEntity(tridentEntity);
                    tridentEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 2.5F + (float) 2 * 0.5F, 1.0F);
                    if (playerEntity.getAbilities().creativeMode) {
                        tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                    }
                    else{
                        user.setStackInHand(user.getActiveHand(),ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}

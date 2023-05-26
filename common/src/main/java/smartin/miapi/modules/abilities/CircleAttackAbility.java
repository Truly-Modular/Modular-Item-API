package smartin.miapi.modules.abilities;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import smartin.miapi.modules.abilities.util.AttackUtil;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.CircleAttackProperty;

public class CircleAttackAbility implements ItemUseAbility {
    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
        return CircleAttackProperty.property.hasCircleAttack(itemStack);
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
        CircleAttackProperty.CircleAttackJson json = CircleAttackProperty.property.get(stack);
        double damage = json.damage;
        double range = json.range;
        double minHold = json.minHold;
        double cooldown = json.cooldown;

        if (user instanceof PlayerEntity player) {
            if (getMaxUseTime(stack) - remainingUseTicks > minHold) {
                damage = ((float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * damage);
                AttackUtil.performSweeping(player, player, (float) range, (float) damage);
                player.swingHand(player.getActiveHand());
                player.getItemCooldownManager().set(stack.getItem(), (int) cooldown);

                if (player.world instanceof ServerWorld serverWorld) {

                    json.particles.forEach(particleJson -> {
                        double radius = range * particleJson.rangePercent; // Set the desired radius for the particle spawn
                        double angleIncrement = 2.0 * Math.asin(0.5 / radius);

                        for (double angle = 0; angle < Math.PI * 2; angle += angleIncrement) {
                            double offsetX = radius * Math.sin(angle);
                            double offsetZ = radius * Math.cos(angle);

                            double particleX = player.getX() + offsetX;
                            double particleY = player.getBodyY(0.5);
                            double particleZ = player.getZ() + offsetZ;
                            particleJson.particleType = Registry.PARTICLE_TYPE.get(new Identifier(particleJson.particle));
                            if (particleJson.particleType instanceof DefaultParticleType particle) {
                                serverWorld.spawnParticles(particle, particleX, particleY, particleZ, particleJson.count, 0, 0, 0, 1.0);
                            }
                        }
                    });
                }
            }
        }
    }
}

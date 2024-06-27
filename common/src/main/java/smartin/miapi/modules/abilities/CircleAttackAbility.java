package smartin.miapi.modules.abilities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.abilities.util.*;
import smartin.miapi.modules.properties.CircleAttackProperty;

/**
 * An ability that attacks everything in a designated radius arround the Caster
 */
public class CircleAttackAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return CircleAttackProperty.property.hasCircleAttack(itemStack);
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if(user.getCooldowns().isOnCooldown(user.getItemInHand(hand).getItem())){
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }


    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        CircleAttackProperty.CircleAttackJson json = CircleAttackProperty.property.get(stack);
        double damage = json.damage;
        double range = json.range;
        double minHold = json.minHold;
        double cooldown = json.cooldown;

        if (user instanceof Player player) {
            if (getMaxUseTime(stack) - remainingUseTicks > minHold) {
                damage = ((float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * damage);
                AttackUtil.performSweeping(player, player, (float) range, (float) damage);
                player.swing(player.getUsedItemHand());
                player.getCooldowns().addCooldown(stack.getItem(), (int) cooldown);

                if (player.level() instanceof ServerLevel serverWorld) {

                    json.particles.forEach(particleJson -> {
                        double radius = range * particleJson.rangePercent; // Set the desired radius for the particle spawn
                        double angleIncrement = 2.0 * Math.asin(0.5 / radius);

                        for (double angle = 0; angle < Math.PI * 2; angle += angleIncrement) {
                            double offsetX = radius * Math.sin(angle);
                            double offsetZ = radius * Math.cos(angle);

                            double particleX = player.getX() + offsetX;
                            double particleY = player.getY(0.5);
                            double particleZ = player.getZ() + offsetZ;
                            particleJson.particleType = BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(particleJson.particle));
                            if (particleJson.particleType instanceof DefaultParticleType particle) {
                                serverWorld.sendParticles(particle, particleX, particleY, particleZ, particleJson.count, 0, 0, 0, 1.0);
                            }
                        }
                    });
                }
            }
        }
    }
}

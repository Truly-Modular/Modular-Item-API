package smartin.miapi.modules.abilities;

import com.redpxnda.nucleus.network.clientbound.ParticleCreationPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import smartin.miapi.mixin.LivingEntityAccessor;
import smartin.miapi.modules.abilities.util.AttackUtil;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.AbilityProperty;
import smartin.miapi.modules.properties.HeavyAttackProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This Ability allows a stronger attack than the normal left click.
 * Has Configurable range and default sweeping and a scale factor for Damage
 */
public class HeavyAttackAbility implements ItemUseAbility {

    public HeavyAttackAbility() {
        if(smartin.miapi.Environment.isClient()){
            clientSetup();
        }
    }

    @Environment(EnvType.CLIENT)
    public void clientSetup(){
        LoreProperty.loreSuppliers.add(itemStack -> {
            List<Text> texts = new ArrayList<>();
            if (AbilityProperty.property.isPrimaryAbility(this, itemStack)) {
                texts.add(Text.translatable("miapi.ability.heavy_attack.lore"));
            }
            return texts;
        });
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityContext abilityContext) {
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
        if (user.getItemCooldownManager().isCoolingDown(user.getStackInHand(hand).getItem())) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }


    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        HeavyAttackProperty.HeavyAttackHolder heavyAttackJson = HeavyAttackProperty.property.get(stack);
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
                    if (player.getWorld() instanceof ServerWorld serverWorld) {
                        if (heavyAttackJson.particle != null) {
                            ParticleCreationPacket particleCreationPacket = new ParticleCreationPacket(heavyAttackJson.particle, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
                            particleCreationPacket.send(serverWorld);
                        }
                    }
                }
            }
        }
    }
}

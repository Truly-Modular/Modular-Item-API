package smartin.miapi.modules.abilities;

import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.AbilityMangerProperty;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * This Ability allows you to throw the Item in question like a Trident
 */
public class ThrowingAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {

    public ThrowingAbility() {
        LoreProperty.bottomLoreSuppliers.add(itemStack -> {
            List<Component> texts = new ArrayList<>();
            if (AbilityMangerProperty.isPrimaryAbility(this, itemStack)) {
                texts.add(Component.translatable("miapi.ability.throw.lore"));
            }
            return texts;
        });
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public int minHoldTimeDefault(){
        return 10;
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof Player playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                playerEntity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                if (!world.isClientSide) {
                    stack.hurtAndBreak(1, playerEntity, (p) -> {
                        p.sendToolBreakStatus(user.getActiveHand());
                    });

                    ItemProjectileEntity projectileEntity = new ItemProjectileEntity(world, playerEntity, stack);
                    float divergence = (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY);
                    float speed = (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED);
                    float damage = (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE);
                    damage = damage / speed;
                    if (stack.getItem() instanceof ModularItem) {
                        speed = 0.5f;
                    }
                    projectileEntity.shootFromRotation(playerEntity, playerEntity.getXRot(), playerEntity.getYRot(), 0.0F, speed, divergence);
                    projectileEntity.setBaseDamage(damage);
                    projectileEntity.setBowItem(ItemStack.EMPTY);
                    projectileEntity.setPierceLevel((byte) (int) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_PIERCING));
                    projectileEntity.setSpeedDamage(true);
                    projectileEntity.setPreferredSlot(playerEntity.getInventory().selected);
                    projectileEntity.thrownStack = stack;
                    world.addFreshEntity(projectileEntity);
                    world.playSound(null, user, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
                    if (playerEntity.getAbilities().instabuild) {
                        projectileEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    } else {
                        user.setItemInHand(user.getUsedItemHand(), ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}

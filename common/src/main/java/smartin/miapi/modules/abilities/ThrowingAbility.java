package smartin.miapi.modules.abilities;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.AbilityMangerProperty;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This Ability allows you to throw the Item in question like a Trident
 */
public class ThrowingAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {

    public ThrowingAbility() {
        LoreProperty.bottomLoreSuppliers.add(itemStack -> {
            List<Text> texts = new ArrayList<>();
            if (AbilityMangerProperty.isPrimaryAbility(this, itemStack)) {
                texts.add(Text.translatable("miapi.ability.throw.lore"));
            }
            return texts;
        });
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
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
    public int minHoldTimeDefault(){
        return 10;
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                playerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                if (!world.isClient) {
                    stack.damage(1, playerEntity, (p) -> {
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
                    projectileEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, speed, divergence);
                    projectileEntity.setDamage(damage);
                    projectileEntity.setBowItem(ItemStack.EMPTY);
                    projectileEntity.setPierceLevel((byte) (int) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_PIERCING));
                    projectileEntity.setSpeedDamage(true);
                    if (user.getActiveHand() == Hand.OFF_HAND) {
                        projectileEntity.setPreferredSlot(-2);
                    } else {
                        projectileEntity.setPreferredSlot(playerEntity.getInventory().selectedSlot);
                    }
                    projectileEntity.thrownStack = stack;
                    world.spawnEntity(projectileEntity);
                    world.playSoundFromEntity(null, user, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    if (playerEntity.getAbilities().creativeMode) {
                        projectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                    } else {
                        user.setStackInHand(user.getActiveHand(), ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}

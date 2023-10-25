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
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.AbilityProperty;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This Ability allows you to throw the Item in question like a Trident
 */
public class ThrowingAbility implements ItemUseAbility {

    public ThrowingAbility() {
        LoreProperty.loreSuppliers.add(itemStack -> {
            List<Text> texts = new ArrayList<>();
            if (AbilityProperty.property.isPrimaryAbility(this, itemStack)) {
                texts.add(Text.translatable("miapi.ability.throw.lore"));
            }
            return texts;
        });
    }
    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityContext abilityContext) {
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

                    ItemProjectileEntity tridentEntity = new ItemProjectileEntity(world, playerEntity, stack);
                    float divergence = (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY);
                    float speed = (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED);
                    float damage = (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE);
                    damage = damage / speed;
                    if (stack.getItem() instanceof ModularItem) {
                        speed = 0.5f;
                    }
                    tridentEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, speed, divergence);
                    tridentEntity.setDamage(damage);
                    tridentEntity.setBowItem(ItemStack.EMPTY);
                    tridentEntity.setPierceLevel((byte) (int) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_PIERCING));
                    tridentEntity.setSpeedDamage(true);
                    tridentEntity.setPreferredSlot(playerEntity.getInventory().selectedSlot);
                    tridentEntity.thrownStack = stack;
                    world.spawnEntity(tridentEntity);
                    world.playSoundFromEntity(null, user, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    if (playerEntity.getAbilities().creativeMode) {
                        tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                    } else {
                        user.setStackInHand(user.getActiveHand(), ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}

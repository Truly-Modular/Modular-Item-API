package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.AbilityProperty;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.projectile.stat.throwable.ThrowDamageProperty;
import smartin.miapi.modules.properties.projectile.stat.throwable.ThrowSpeedProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

/**
 * This Ability allows you to throw the Item in question like a Trident
 */
public class ThrowingAbility implements ItemUseDefaultCooldownAbility<ThrowingAbility.BasicContext>, ItemUseMinHoldAbility<ThrowingAbility.BasicContext> {
    public static Codec<BasicContext> CODEC = AutoCodec.of(BasicContext.class).codec();

    public ThrowingAbility() {
        LoreProperty.bottomLoreSuppliers.add(itemStack -> {
            List<Component> texts = new ArrayList<>();
            if (AbilityProperty.isPrimaryAbility(this, itemStack)) {
                texts.add(Component.translatable("miapi.ability.throw.lore"));
            }
            return texts;
        });
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, BasicContext context) {
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack, BasicContext context) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity entity, BasicContext context) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, BasicContext context) {
        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public Codec<BasicContext> getCodec() {
        return CODEC;
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, BasicContext context) {
        if (user instanceof Player playerEntity) {
            int i = this.getMaxUseTime(stack, user, context) - remainingUseTicks;
            if (i >= 10) {
                playerEntity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                if (world instanceof ServerLevel) {
                    EquipmentSlot equipmentSlot = getEquipmentSlot(user.getUsedItemHand());
                    stack.hurtAndBreak(1, playerEntity, equipmentSlot);
                    float speed = (float) ThrowSpeedProperty.getThrowSpeed(stack);
                    float damage = (float) ThrowDamageProperty.getDamage(stack);

                    ItemProjectileEntity projectileEntity = new ItemProjectileEntity(world, playerEntity, stack, stack);
                    damage = damage / speed;
                    if (stack.has(ModularItem.IS_VISUAL_ONLY)) {
                        projectileEntity.setPickupItem(ModularItem.convertToBroken(stack));
                    }
                    projectileEntity.shootFromRotation(playerEntity, playerEntity.getXRot(), playerEntity.getYRot(), 0.0F, speed, 1);
                    projectileEntity.setBaseDamage(damage);
                    projectileEntity.setSpeedDamage(true);
                    if (user.getUsedItemHand() == InteractionHand.OFF_HAND) {
                        projectileEntity.setPreferredSlot(-2);
                    } else {
                        projectileEntity.setPreferredSlot(playerEntity.getInventory().selected);
                    }
                    projectileEntity.thrownStack = stack;
                    world.addFreshEntity(projectileEntity);
                    world.playSound(null, user, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    if (playerEntity.getAbilities().instabuild) {
                        projectileEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    } else {
                        user.setItemInHand(user.getUsedItemHand(), ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public BasicContext initialize(BasicContext data, ModuleInstance moduleInstance) {
        data.cooldown.initialize(moduleInstance);
        data.minUseTime.initialize(moduleInstance);
        return data;
    }

    @Override
    public int getCooldown(ItemStack itemstack) {
        return (int) getSpecialContext(itemstack).cooldown.getValue();
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).minUseTime.getValue();
    }

    @Override
    public BasicContext getDefaultContext() {
        return null;
    }

    @Override
    public BasicContext merge(BasicContext right, BasicContext left, MergeType mergeType) {
        BasicContext context = new BasicContext();
        context.minUseTime = left.minUseTime.merge(right.minUseTime, mergeType);
        context.cooldown = left.cooldown.merge(right.cooldown, mergeType);
        return context;
    }

    public static class BasicContext {
        @AutoCodec.Name("min_hold_time")
        @CodecBehavior.Optional
        public DoubleOperationResolvable minUseTime = new DoubleOperationResolvable(0);
        @CodecBehavior.Optional
        public DoubleOperationResolvable cooldown = new DoubleOperationResolvable(0);
    }
}

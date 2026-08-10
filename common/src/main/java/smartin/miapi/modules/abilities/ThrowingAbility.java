package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import org.jetbrains.annotations.Nullable;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.abilities.util.AbilityProperty;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.projectile.stat.throwable.ThrowDamageProperty;
import smartin.miapi.modules.properties.projectile.stat.throwable.ThrowSpeedProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThrowingAbility implements ItemUseAbility<ThrowingAbilityContext> {
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
    public Codec<ThrowingAbilityContext> getCodec() {
        return ThrowingAbilityContext.CODEC;
    }

    @Override
    public boolean allowedOnItem(
            ItemStack itemStack,
            Level world,
            Player player,
            InteractionHand hand,
            ItemAbilityManager.AbilityHitContext abilityHitContext,
            ThrowingAbilityContext context
    ) {
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack, ThrowingAbilityContext context) {
        return Optional.ofNullable(context.animation)
                .map(name -> {
                    try {
                        return UseAnim.valueOf(name.toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                        return UseAnim.SPEAR;
                    }
                })
                .orElse(UseAnim.SPEAR);
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity, ThrowingAbilityContext context) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, ThrowingAbilityContext context) {
        user.startUsingItem(hand);
        setAnimation(user, context.poseCharge, hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    public void setAnimation(LivingEntity p, @Nullable String pose, InteractionHand hand) {
        if (p instanceof Player pl) {
            setAnimation(pl, pose, hand);
        }
    }

    public void setAnimation(Player p, @Nullable String pose, InteractionHand hand) {
        if (pose == null) {
            return;
        }
        if (p instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null) {
                facet.set(pose, player, hand);
            }
        }
    }

    public void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null)
                facet.reset(player);
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, ThrowingAbilityContext context) {
        int useTime = getMaxUseTime(stack, user, context) - remainingUseTicks;
        if (useTime >= context.minUseTime.getValue()) {
            throwItem(stack, world, user, context);
            resetAnimation(user);
            setAnimation(user, context.poseThrow, InteractionHand.MAIN_HAND);
        } else {
            resetAnimation(user);
        }
    }

    @Override
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user, ThrowingAbilityContext context) {
        resetAnimation(user);
    }

    @Override
    public void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks, ThrowingAbilityContext context) {
        if (!Optional.ofNullable(context.autoRelease).orElse(false)) {
            return;
        }

        int useTime = getMaxUseTime(stack, user, context) - remainingUseTicks;
        if (useTime == (int) context.minUseTime.getValue()) {
            user.releaseUsingItem();
        }
    }

    private void throwItem(ItemStack originalStack, Level world, LivingEntity user, ThrowingAbilityContext context) {
        if (!(user instanceof Player player)) {
            return;
        }

        if (!(world instanceof ServerLevel)) {
            return;
        }

        player.awardStat(Stats.ITEM_USED.get(originalStack.getItem()));

        EquipmentSlot equipmentSlot = getEquipmentSlot(user.getUsedItemHand());

        ItemStack thrownStack = originalStack.copy();
        thrownStack.setCount(1);

        int cooldown = (int) context.cooldown.getValue();
        if (cooldown > 0) {
            player.getCooldowns().addCooldown(originalStack.getItem(), cooldown);
        }
        if (!player.hasInfiniteMaterials()) {
            originalStack.shrink(1);
        }

        thrownStack.hurtAndBreak(1, player, equipmentSlot);

        float speed = (float) ThrowSpeedProperty.getThrowSpeed(thrownStack);
        float damage = (float) ThrowDamageProperty.getDamage(thrownStack);

        ItemProjectileEntity projectile = new ItemProjectileEntity(world, player, thrownStack, thrownStack);
        projectile.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                0.0F,
                speed,
                1.0F
        );

        if (!context.throwPosition.equals(Transform.IDENTITY)) {
            Transform entityTransform = Transform.getTransform(projectile);
            Transform transformed = entityTransform.merge(context.throwPosition);
            Transform.setTransform(projectile, transformed);
        }

        projectile.setBaseDamage(damage / speed);
        projectile.setSpeedDamage(true);
        projectile.thrownStack = thrownStack;

        if (thrownStack.has(ModularItem.IS_VISUAL_ONLY)) {
            projectile.setPickupItem(ModularItem.convertToBroken(thrownStack));
        }

        if (user.getUsedItemHand() == InteractionHand.OFF_HAND) {
            projectile.setPreferredSlot(-2);
        } else {
            projectile.setPreferredSlot(player.getInventory().selected);
        }

        if (player.getAbilities().instabuild) {
            projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        } else {
            user.setItemInHand(user.getUsedItemHand(), ItemStack.EMPTY);
        }

        world.addFreshEntity(projectile);

        world.playSound(
                null,
                user,
                SoundEvents.TRIDENT_THROW.value(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    @Override
    public ThrowingAbilityContext getDefaultContext() {
        return new ThrowingAbilityContext();
    }

    @Override
    public ThrowingAbilityContext initialize(ThrowingAbilityContext data, smartin.miapi.modules.ModuleInstance moduleInstance) {
        return data.initialize(moduleInstance);
    }

    @Override
    public ThrowingAbilityContext merge(ThrowingAbilityContext left, ThrowingAbilityContext right, smartin.miapi.modules.properties.util.MergeType mergeType) {
        return left.merge(right, mergeType);
    }

    @Override
    public boolean useOnRelease(ItemStack itemStack, ThrowingAbilityContext context) {
        return true;
    }
}
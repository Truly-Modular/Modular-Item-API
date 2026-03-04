package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import smartin.miapi.MixinContextFlags;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.item.modular.items.bows.ModularCrossbow;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.projectile.stat.bow.BowDrawTimeProperty;

import java.util.List;
import java.util.function.Supplier;

public class CrossbowAbility implements ItemUseAbility<CrossbowAbility.Context> {

    public static record Context(boolean dummy) { } // You can expand with config data if needed

    public static final Codec<Context> CODEC = Codec.BOOL
            .optionalFieldOf("dummy", false)
            .xmap(Context::new, Context::dummy).codec();

    @Override
    public Codec<Context> getCodec() {
        return CODEC;
    }

    @Override
    public boolean allowedOnItem(ItemStack stack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, Context context) {
        return true; // always allow for crossbow modules
    }

    @Override
    public UseAnim getUseAction(ItemStack stack, Context context) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity entity, Context context) {
        return getChargeDuration(stack, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand, Context context) {
        ItemStack crossbow = player.getItemInHand(hand);
        ChargedProjectiles charged = crossbow.get(DataComponents.CHARGED_PROJECTILES);

        if (charged != null && !charged.isEmpty()) {
            if (MiapiProjectileEvents.MODULAR_CROSSBOW_PRE_SHOT.invoker().shoot(player, crossbow).interruptsFurtherEvaluation()) {
                return InteractionResultHolder.consume(crossbow);
            }

            player.releaseUsingItem(); // triggers shooting logic
            if (MiapiProjectileEvents.MODULAR_CROSSBOW_POST_SHOT.invoker().shoot(player, crossbow).interruptsFurtherEvaluation()) {
                return InteractionResultHolder.consume(crossbow);
            }

            return InteractionResultHolder.consume(crossbow);

        } else {
            // Not charged → start pulling
            if (!player.getProjectile(crossbow).isEmpty()) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(crossbow);
            }
            return InteractionResultHolder.fail(crossbow);
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks, Context context) {
        int ticks = getMaxUseTime(stack, user, context) - remainingUseTicks;
        float charge = getPowerForTime(ticks, stack, user);

        MiapiProjectileEvents.CrossbowLoadingContext ctx =
                new MiapiProjectileEvents.CrossbowLoadingContext(user, stack, user.getProjectile(stack), EquipmentSlot.MAINHAND);

        if (charge >= 1.0F &&
            !CrossbowItem.isCharged(stack) &&
            !MiapiProjectileEvents.MODULAR_CROSSBOW_PRE_LOAD.invoker().load(ctx).interruptsFurtherEvaluation() &&
            tryLoadProjectiles(user, stack)) {

            if (MiapiProjectileEvents.MODULAR_CROSSBOW_POST_LOAD.invoker().load(ctx).interruptsFurtherEvaluation()) {
                return;
            }
        }
    }

    private static boolean tryLoadProjectiles(LivingEntity shooter, ItemStack crossbowStack) {
        List<ItemStack> list = ModularCrossbow.drawPublic(crossbowStack, shooter.getProjectile(crossbowStack), shooter);
        if (!list.isEmpty()) {
            crossbowStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(list));
            return true;
        } else {
            return false;
        }
    }

    /* ----------------- Utility methods migrated from ModularCrossbow ------------------ */

    private static float getPowerForTime(int timeLeft, ItemStack stack, LivingEntity shooter) {
        float f = (float) timeLeft / (float) getChargeDuration(stack, shooter);
        return Math.min(f, 1.0F);
    }

    private static int getChargeDuration(ItemStack stack, LivingEntity shooter) {
        double drawTime = BowDrawTimeProperty.property.getValue(stack).orElse(0.25);
        float f = EnchantmentHelper.modifyCrossbowChargingTime(stack, shooter, (float) drawTime);
        return (int) (f * 20.0F);
    }

    private static float getShootingPower(ChargedProjectiles projectile) {
        return projectile.contains(Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    /* ----------- Example of withFlag usage when fetching ammo ----------- */
    private static <T> T withFlag(ItemStack stack, CopyItemAbility.ItemContext item, Supplier<T> action) {
        if (item == null) return null;
        try {
            if (item.fakeItemIdentity) {
                MixinContextFlags.IGNORE_NEXT_GET_ITEM_CALL.get().put(stack, item.item);
            }
            return action.get();
        } finally {
            if (item.fakeItemIdentity) {
                MixinContextFlags.IGNORE_NEXT_GET_ITEM_CALL.get().remove(stack);
            }
        }
    }
}

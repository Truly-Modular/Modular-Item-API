package smartin.miapi.item.modular.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.lwjgl.system.NonnullDefault;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MeleeModularAttackEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.FakeItemManager;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.item.modular.items.ModularToolMaterial;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.RepairPriority;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;

import java.util.List;

@NonnullDefault
public class ModularSword extends SwordItem implements PlatformModularItemMethods, ModularItem,ProjectileItem {

    public ModularSword(Properties settings) {
        super(new ModularToolMaterial(), settings.stacksTo(1).durability(500));
    }

    public ModularSword() {
        super(new ModularToolMaterial(), new Properties().stacksTo(1).durability(500).rarity(Rarity.COMMON));
    }

    @Override
    public ItemStack getDefaultInstance() {
        return FakeItemManager.getDefaultInstance(this);
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        ComponentApplyProperty.initializeItemStack(stack, null);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
        MutableFloat mutableFloat = new MutableFloat(0);
        if (damageSource.getWeaponItem() != null) {
            MeleeModularAttackEvents.ATTACK_DAMAGE_BONUS.invoker().getAttackDamageBonus(target, damageSource.getWeaponItem(), damage, damageSource, mutableFloat);
        }
        return mutableFloat.floatValue();
    }

    @Override
    public Projectile asProjectile(Level world, Position position, ItemStack stack, Direction direction) {
        ItemStack itemStack = stack.copy();
        itemStack.setCount(1);
        ItemProjectileEntity arrowEntity = new ItemProjectileEntity(world, position, itemStack);
        arrowEntity.setPosRaw(position.x(), position.y(), position.z());
        arrowEntity.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrowEntity;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return !MeleeModularAttackEvents.HURT_ENEMY.invoker().hurtEnemy(stack, target, attacker).interruptsFurtherEvaluation();
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        MeleeModularAttackEvents.HURT_ENEMY_POST.invoker().hurtEnemy(stack, target, attacker);
    }

    @Override
    public Tier getTier() {
        ItemStack itemStack = FakeItemManager.getDefaultInstance(this);
        if (MiapiConfig.getServerConfig().other.looseToolMaterial && itemStack != null) {
            return ModularToolMaterial.forItemStack(itemStack);
        }
        return super.getTier();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - stack.getDamageValue() * 13.0F / ModularItem.getDurability(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) ModularItem.getDurability(stack) - stack.getDamageValue()) / ModularItem.getDurability(stack));
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        MiapiEvents.INVENTORY_TICK.invoker().tick(stack,level,entity,slotId,isSelected);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    @Override
    public Component getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public int getEnchantmentValue() {
        ItemStack itemStack = FakeItemManager.getLastInstance(this);
        if (itemStack != null) {
            return (int) EnchantAbilityProperty.getEnchantAbility(itemStack);
        }
        return 15;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return ItemAbilityManager.getUseAction(stack, () -> super.getUseAnimation(stack));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return ItemAbilityManager.getMaxUseTime(stack, entity, () -> super.getUseDuration(stack, entity));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return ItemAbilityManager.use(world, user, hand, () -> super.use(world, user, hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        ItemAbilityManager.onStoppedUsing(stack, world, user, remainingUseTicks, () -> super.releaseUsing(stack, world, user, remainingUseTicks));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        return ItemAbilityManager.finishUsing(stack, world, user, () -> super.finishUsingItem(stack, world, user));
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        ItemAbilityManager.usageTick(world, user, stack, remainingUseTicks, () -> super.onUseTick(world, user, stack, remainingUseTicks));
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return ItemAbilityManager.useOnRelease(stack, () -> super.useOnRelease(stack));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        return ItemAbilityManager.useOnEntity(stack, user, entity, hand, () -> super.interactLivingEntity(stack, user, entity, hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return ItemAbilityManager.useOnBlock(context, () -> super.useOn(context));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipType) {
        LoreProperty.appendLoreTop(stack, list, tooltipContext, tooltipType);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return MiningLevelProperty.getDestroySpeed(stack, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        return MiningLevelProperty.mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return MiningLevelProperty.isCorrectToolForDrops(stack, state);
    }
}

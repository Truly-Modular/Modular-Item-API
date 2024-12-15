package smartin.miapi.item.modular.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.lwjgl.system.NonnullDefault;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.events.ModularAttackEvents;
import smartin.miapi.item.FakeItemstackReferenceProvider;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.item.modular.items.ModularSetableToolMaterial;
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
public class ModularHoe extends HoeItem implements PlatformModularItemMethods, ModularItem, ModularSetableToolMaterial {
    public Tier currentFakeToolmaterial = ModularToolMaterial.toolMaterial;

    public ModularHoe(Properties settings) {
        super(new ModularToolMaterial(), settings.stacksTo(1).durability(500));
    }

    public ModularHoe() {
        super(new ModularToolMaterial(), new Properties().stacksTo(1).durability(500).rarity(Rarity.COMMON));
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        ComponentApplyProperty.updateItemStack(stack, Miapi.registryAccess);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
        MutableFloat mutableFloat = new MutableFloat(0);
        if (damageSource.getWeaponItem() != null) {
            ModularAttackEvents.ATTACK_DAMAGE_BONUS.invoker().getAttackDamageBonus(target, damageSource.getWeaponItem(), damage, damageSource, mutableFloat);
        }
        return mutableFloat.floatValue();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return ModularAttackEvents.HURT_ENEMY.invoker().hurtEnemy(stack, target, attacker).interruptsFurtherEvaluation();
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        ModularAttackEvents.HURT_ENEMY_POST.invoker().hurtEnemy(stack, target, attacker);
    }

    @Override
    public Tier getTier() {
        if(MiapiConfig.INSTANCE.server.other.looseToolMaterial){
            return currentFakeToolmaterial;
        }
        return super.getTier();
    }

    @Override
    public void setToolMaterial(Tier toolMaterial){
        this.currentFakeToolmaterial = toolMaterial;
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
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    @Override
    public Component getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public int getEnchantmentValue() {
        ItemStack itemStack = FakeItemstackReferenceProvider.getFakeReference(this);
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

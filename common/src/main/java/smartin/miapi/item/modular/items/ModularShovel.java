package smartin.miapi.item.modular.items;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.List;

public class ModularShovel extends ShovelItem implements PlatformModularItemMethods, ModularItem, ModularSetableToolMaterial {
    public Tier currentFakeToolmaterial = ModularToolMaterial.toolMaterial;

    public ModularShovel(Properties settings) {
        super(new ModularToolMaterial(), 5, 5, settings.stacksTo(1).durability(500));
    }

    public ModularShovel() {
        super(new ModularToolMaterial(), 5, 5, new Properties().stacksTo(1).durability(500).rarity(Rarity.COMMON));
    }

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
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / ModularItem.getDurability(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) ModularItem.getDurability(stack) - (float) stack.getDamageValue()) / ModularItem.getDurability(stack));
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
        return 1;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (ToolOrWeaponProperty.isWeapon(stack)) {
            stack.hurtAndBreak(1, attacker, (e) -> {
                e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
            });
        } else {
            stack.hurtAndBreak(2, attacker, (e) -> {
                e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
            });
        }
        return true;
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return ArrayListMultimap.create();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClientSide && state.getDestroySpeed(world, pos) != 0.0F) {
            if (ToolOrWeaponProperty.isWeapon(stack)) {
                stack.hurtAndBreak(2, miner, (e) -> {
                    e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                });
            } else {
                stack.hurtAndBreak(1, miner, (e) -> {
                    e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                });
            }
        }

        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        return MiningLevelProperty.canMine(state, world, pos, miner);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return MiningLevelProperty.getMiningSpeedMultiplier(stack, state);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return ItemAbilityManager.getUseAction(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return ItemAbilityManager.getMaxUseTime(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        return ItemAbilityManager.use(world, user, hand);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        ItemAbilityManager.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        return ItemAbilityManager.finishUsing(stack, world, user);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return RarityProperty.getRarity(stack);
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        ItemAbilityManager.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        //TODO;
        return true;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        return ItemAbilityManager.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return ItemAbilityManager.useOnBlock(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipType) {
        LoreProperty.appendLoreTop(stack, list, tooltipContext, tooltipType);
    }
}

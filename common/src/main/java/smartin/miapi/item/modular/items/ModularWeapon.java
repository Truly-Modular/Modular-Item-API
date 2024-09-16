package smartin.miapi.item.modular.items;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.List;

public class ModularWeapon extends Item implements PlatformModularItemMethods, ModularItem {
    public ModularWeapon() {
        this(new Settings(), true);
    }

    public ModularWeapon(Settings settings, boolean withDefaultSettings) {
        super(withDefaultSettings ? settings.maxCount(1).maxDamage(500) : settings);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamage() * 13.0F / ModularItem.getDurability(stack));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) ModularItem.getDurability(stack) - (float) stack.getDamage()) / ModularItem.getDurability(stack));
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    @Override
    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public int getEnchantability() {
        return 1;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (ToolOrWeaponProperty.isWeapon(stack)) {
            stack.damage(1, attacker, (e) -> {
                e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
            });
        } else {
            stack.damage(2, attacker, (e) -> {
                e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
            });
        }
        return true;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && state.getHardness(world, pos) != 0.0F) {
            if (ToolOrWeaponProperty.isWeapon(stack)) {
                stack.damage(2, miner, (e) -> {
                    e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                });
            } else {
                stack.damage(1, miner, (e) -> {
                    e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                });
            }
        }

        return true;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return MiningLevelProperty.canMine(state, world, pos, miner);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return MiningLevelProperty.getMiningSpeedMultiplier(stack, state);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return ItemAbilityManager.getUseAction(stack, () -> super.getUseAction(stack));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return ItemAbilityManager.getMaxUseTime(stack, () -> super.getMaxUseTime(stack));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemAbilityManager.use(world, user, hand, () -> super.use(world, user, hand));
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        ItemAbilityManager.onStoppedUsing(stack, world, user, remainingUseTicks, () -> super.onStoppedUsing(stack, world, user, remainingUseTicks));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        return ItemAbilityManager.finishUsing(stack, world, user, () -> finishUsing(stack, world, user));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return RarityProperty.getRarity(stack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        ItemAbilityManager.usageTick(world, user, stack, remainingUseTicks, () -> super.usageTick(world, user, stack, remainingUseTicks));
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return ItemAbilityManager.isUsedOnRelease(stack,() -> super.isUsedOnRelease(stack));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return ItemAbilityManager.useOnEntity(stack, user, entity, hand, () -> super.useOnEntity(stack, user, entity, hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ItemAbilityManager.useOnBlock(context, () -> super.useOnBlock(context));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        LoreProperty.appendLoreTop(stack, world, tooltip, context);
    }
}

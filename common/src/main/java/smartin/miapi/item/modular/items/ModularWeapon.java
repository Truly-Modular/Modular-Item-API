package smartin.miapi.item.modular.items;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.MiningLevelProperty;
import smartin.miapi.modules.properties.ToolOrWeaponProperty;

public class ModularWeapon extends Item implements ModularItem {
    public ModularWeapon() {
        super(new Settings().fireproof().maxCount(1).maxDamage(500).rarity(Rarity.COMMON));
    }

    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return MiningLevelProperty.canMine(state, world, pos, miner);
    }

    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return MiningLevelProperty.getMiningSpeedMultiplier(stack, state);
    }

    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if(ToolOrWeaponProperty.isWeapon(stack)){
            stack.damage(1,attacker, (e) -> {
                e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
            });
        }
        else {
            stack.damage(2, attacker, (e) -> {
                e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
            });
        }
        return true;
    }

    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && state.getHardness(world, pos) != 0.0F) {
            if(ToolOrWeaponProperty.isWeapon(stack)){
                stack.damage(2, miner, (e) -> {
                    e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                });
            }
            else {
                stack.damage(1, miner, (e) -> {
                    e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                });
            }
        }

        return true;
    }


    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    public UseAction getUseAction(ItemStack stack) {
        return ItemAbilityManager.getUseAction(stack);
    }

    public int getMaxUseTime(ItemStack stack) {
        return ItemAbilityManager.getMaxUseTime(stack);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemAbilityManager.use(world, user, hand);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        ItemAbilityManager.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        return ItemAbilityManager.finishUsing(stack, world, user);
    }

    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        ItemAbilityManager.usageTick(world, user, stack, remainingUseTicks);
    }

    public boolean isUsedOnRelease(ItemStack stack) {
        //TODO;
        return true;
    }

    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return ItemAbilityManager.useOnEntity(stack, user, entity, hand);
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        return ItemAbilityManager.useOnBlock(context);
    }
}
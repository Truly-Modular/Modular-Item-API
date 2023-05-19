package smartin.miapi.item.modular.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import smartin.miapi.item.modular.ItemAbilityManager;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;

public class ModularWeapon extends Item implements ModularItem {
    public ModularWeapon() {
        super(new Settings().fireproof().maxCount(1).maxDamage(500).rarity(Rarity.COMMON));
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

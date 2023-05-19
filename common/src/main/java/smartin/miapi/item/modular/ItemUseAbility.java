package smartin.miapi.item.modular;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public interface ItemUseAbility {
    boolean allowedOnItem(ItemStack itemStack);

    UseAction getUseAction(ItemStack itemStack);

    int getMaxUseTime(ItemStack itemStack);

    UseAction onUse(World world, PlayerEntity user, Hand hand);

    TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand);

    ItemStack finishUsing(ItemStack stack, World world, LivingEntity user);

    void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks);

    void onStoppedHolding(ItemStack stack, World world, LivingEntity user);

    ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand);

    ActionResult useOnBlock(ItemUsageContext context);
}

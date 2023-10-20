package smartin.miapi.modules.abilities.toolabilities;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.mixin.HoeItemAccessor;
import smartin.miapi.modules.abilities.ToolAbilities;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HoeAbility extends ToolAbilities {
    public final static String KEY = "hoe_ability";

    @Override
    public Optional<BlockState> getBlockState(BlockState blockState, ItemUsageContext context) {
        HoeItem hoeItem;
        return Optional.empty();
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        Pair<Predicate<ItemUsageContext>, Consumer<ItemUsageContext>> pair = HoeItemAccessor.getTILLING_ACTIONS().get(world.getBlockState(blockPos).getBlock());
        if (pair == null) {
            return ActionResult.PASS;
        } else {
            Predicate<ItemUsageContext> predicate = (Predicate) pair.getFirst();
            Consumer<ItemUsageContext> consumer = (Consumer) pair.getSecond();
            if (predicate.test(context)) {
                PlayerEntity playerEntity = context.getPlayer();
                world.playSound(playerEntity, blockPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!world.isClient) {
                    consumer.accept(context);
                    if (playerEntity != null) {
                        context.getStack().damage(1, playerEntity, (p) -> {
                            p.sendToolBreakStatus(context.getHand());
                        });
                    }
                }

                return ActionResult.success(world.isClient);
            } else {
                return ActionResult.PASS;
            }
        }
    }
}

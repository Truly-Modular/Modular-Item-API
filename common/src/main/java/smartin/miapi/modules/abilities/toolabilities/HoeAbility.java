package smartin.miapi.modules.abilities.toolabilities;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.mixin.HoeItemAccessor;
import smartin.miapi.modules.abilities.util.ToolAbilities;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HoeAbility extends ToolAbilities {
    public final static String KEY = "hoe_ability";

    @Override
    public Optional<BlockState> getBlockState(BlockState blockState, UseOnContext context) {
        HoeItem hoeItem;
        return Optional.empty();
    }

    public InteractionResult useOnBlock(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = HoeItemAccessor.getTILLING_ACTIONS().get(world.getBlockState(blockPos).getBlock());
        if (pair == null) {
            return InteractionResult.PASS;
        } else {
            Predicate<UseOnContext> predicate = (Predicate) pair.getFirst();
            Consumer<UseOnContext> consumer = (Consumer) pair.getSecond();
            if (predicate.test(context)) {
                Player playerEntity = context.getPlayer();
                world.playSound(playerEntity, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!world.isClientSide) {
                    consumer.accept(context);
                    if (playerEntity != null) {
                        context.getItemInHand().hurtAndBreak(1, playerEntity, getEquipmentSlot(context.getHand()));
                    }
                }

                return InteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }
}

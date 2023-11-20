package smartin.miapi.mixin;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.events.MiapiEvents;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "dropExperienceWhenMined(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/intprovider/IntProvider;)V", at = @At("TAIL"))
    private void miapi$startRidingEvent(ServerWorld world, BlockPos pos, ItemStack tool, IntProvider experience, CallbackInfo ci) {
        MiapiEvents.BLOCK_BREAK_EVENT.invoker().breakBlock(world, pos, tool, experience);
    }
}

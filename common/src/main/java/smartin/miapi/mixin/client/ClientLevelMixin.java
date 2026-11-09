package smartin.miapi.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.module.dynamic.thread.WorldPhysicsAccess;

@Mixin(ClientPacketListener.class)
public abstract class ClientLevelMixin {

    @Shadow public abstract PotionBrewing potionBrewing();

    @Inject(method = "handleBlockUpdate", at = @At("TAIL"))
    private void onBlockUpdate(
            ClientboundBlockUpdatePacket packet,
            CallbackInfo ci
    ) {
        BlockPos pos = packet.getPos();
        BlockState state = packet.getBlockState();
        WorldPhysicsAccess.CLIENT_WORLD.blockUpdate(Minecraft.getInstance().level,pos,state);

        // Handle block update
    }
}
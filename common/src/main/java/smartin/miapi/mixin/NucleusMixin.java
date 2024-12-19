package smartin.miapi.mixin;

import com.redpxnda.nucleus.Nucleus;
import com.redpxnda.nucleus.network.NucleusPacket;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Nucleus.class)
public class NucleusMixin {

    @Inject(method = "registerPacket(Ldev/architectury/networking/NetworkManager$Side;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$Type;Lnet/minecraft/network/codec/StreamCodec;)V", at = @At(value = "HEAD"), cancellable = true)
    private static <T extends NucleusPacket> void miapi$registerPacket(NetworkManager.Side side, CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, CallbackInfo ci) {
        if (side == NetworkManager.Side.S2C && !(Platform.getEnvironment() == Env.CLIENT)) {
            NetworkManager.registerS2CPayloadType(type, streamCodec);
        }else{
            NetworkManager.registerReceiver(side, type, streamCodec, (packet, context) -> context.queue(() -> packet.handle(context)));
        }
        ci.cancel();
    }
}
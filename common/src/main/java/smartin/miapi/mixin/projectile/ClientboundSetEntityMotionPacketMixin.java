package smartin.miapi.mixin.projectile;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetEntityMotionPacket.class)
public class ClientboundSetEntityMotionPacketMixin {

    @Shadow @Final @Mutable
    public static StreamCodec<FriendlyByteBuf, ClientboundSetEntityMotionPacket> STREAM_CODEC;

    // Store the original double velocities
    @Unique private double originalX;
    @Unique private double originalY;
    @Unique private double originalZ;

    /**
     * Capture doubles before they're scaled down to shorts.
     */
    @Inject(
            method = "<init>(ILnet/minecraft/world/phys/Vec3;)V",
            at = @At("TAIL")
    )
    private void onConstruct(int id, Vec3 deltaMovement, CallbackInfo ci) {
        this.originalX = deltaMovement.x;
        this.originalY = deltaMovement.y;
        this.originalZ = deltaMovement.z;
    }

    /**
     * Replace STREAM_CODEC with one that uses doubles.
     */
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void replaceCodec(CallbackInfo ci) {
        STREAM_CODEC = StreamCodec.of(
                // Writer
                (buf, packet) -> {
                    buf.writeVarInt(packet.getId());
                    buf.writeDouble(((ClientboundSetEntityMotionPacketMixin)(Object)packet).originalX);
                    buf.writeDouble(((ClientboundSetEntityMotionPacketMixin)(Object)packet).originalY);
                    buf.writeDouble(((ClientboundSetEntityMotionPacketMixin)(Object)packet).originalZ);
                },
                // Reader
                (buf) -> new ClientboundSetEntityMotionPacket(
                        buf.readVarInt(),
                        new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
                )
        );
    }

    @ModifyReturnValue(
            method = "Lnet/minecraft/network/protocol/game/ClientboundSetEntityMotionPacket;getXa()D",
            at = @At("RETURN"),
            remap = true,
            require = -1)
    private double miapi$fixvelX(double original) {
        return this.originalX;
    }

    @ModifyReturnValue(
            method = "Lnet/minecraft/network/protocol/game/ClientboundSetEntityMotionPacket;getYa()D",
            at = @At("RETURN"),
            remap = true,
            require = -1)
    private double miapi$fixvelY(double original) {
        return this.originalY;
    }

    @ModifyReturnValue(
            method = "Lnet/minecraft/network/protocol/game/ClientboundSetEntityMotionPacket;getZa()D",
            at = @At("RETURN"),
            remap = true,
            require = -1)
    private double miapi$fixvelZ(double original) {
        return this.originalZ;
    }
}

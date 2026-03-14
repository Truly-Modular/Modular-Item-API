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
                    double x = ((ClientboundSetEntityMotionPacketMixin)(Object)packet).originalX;
                    double y = ((ClientboundSetEntityMotionPacketMixin)(Object)packet).originalY;
                    double z = ((ClientboundSetEntityMotionPacketMixin)(Object)packet).originalZ;

                    int modeX = chooseMode(x);
                    int modeY = chooseMode(y);
                    int modeZ = chooseMode(z);

                    int header =
                            (modeX) |
                            (modeY << 2) |
                            (modeZ << 4);
                    buf.writeVarInt(packet.getId());
                    buf.writeByte(header);

                    writeAxis(buf, modeX, x);
                    writeAxis(buf, modeY, y);
                    writeAxis(buf, modeZ, z);
                    /*
                    buf.writeVarInt(packet.getId());
                    buf.writeDouble(((ClientboundSetEntityMotionPacketMixin)(Object)packet).originalX);
                    buf.writeDouble(((ClientboundSetEntityMotionPacketMixin)(Object)packet).originalY);
                    buf.writeDouble(((ClientboundSetEntityMotionPacketMixin)(Object)packet).originalZ);
                     */
                },
                // Reader

                (buf) -> {
                    int type = buf.readVarInt();
                    int header = buf.readUnsignedByte();

                    int modeX = header & 0b11;
                    int modeY = (header >> 2) & 0b11;
                    int modeZ = (header >> 4) & 0b11;

                    double x = readAxis(buf, modeX);
                    double y = readAxis(buf, modeY);
                    double z = readAxis(buf, modeZ);
                    return new ClientboundSetEntityMotionPacket(
                            type,
                            new Vec3(x, y, z)
                    );
                }
        );
    }

    private static int chooseMode(double v) {
        double a = Math.abs(v);

        if (a < 1e-4) return 0;
        if (a < 0.5)  return 1;
        if (a < 4)    return 2;
        return 3;
    }

    private static void writeAxis(FriendlyByteBuf buf, int mode, double v) {
        switch (mode) {
            case 1:
                buf.writeByte((int)(v * 256));
                break;

            case 2:
                buf.writeShort((int)(v * 4096));
                break;

            case 3:
                buf.writeFloat((float)v);
                break;
        }
    }


    private static double readAxis(FriendlyByteBuf buf, int mode) {
        switch (mode) {
            case 0:
                return 0;

            case 1:
                return buf.readByte() / 256.0;

            case 2:
                return buf.readShort() / 4096.0;

            case 3:
                return buf.readFloat();

            default:
                return 0;
        }
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

package smartin.miapi.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import smartin.miapi.Miapi;
import smartin.miapi.network.Networking;
import smartin.miapi.network.modern.payload.CustomPayload;

import java.util.UUID;

public record S2CMiapiOLDPayload(CustomPayload payload) implements CustomPacketPayload {
    public static UUID noPlayerUUID = UUID.fromString("ddfe3f2c-2d4e-4242-8a65-f4641ba9f5f6");

    public static final StreamCodec<FriendlyByteBuf, S2CMiapiOLDPayload> STREAM_CODEC = CustomPacketPayload.codec(S2CMiapiOLDPayload::encode, S2CMiapiOLDPayload::decode);

    public static final Type<S2CMiapiOLDPayload> TYPE = new Type<>(Miapi.id("default-s2c-networking-old"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static S2CMiapiOLDPayload decode(FriendlyByteBuf friendlyByteBuf) {
        CustomPayload payload = CustomPayload.decode(friendlyByteBuf);
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeBytes(payload.data());
        if (!Networking.S2CPackets.containsKey(payload.id())) {
            Miapi.LOGGER.error("no reciever for s2c " + payload.id() + " was registered");
        } else {
            Networking.S2CPackets.get(payload.id()).accept(buf);
        }
        return new S2CMiapiOLDPayload(payload);
    }

    public void encode(FriendlyByteBuf data) {
        payload().encode(data);
    }
}

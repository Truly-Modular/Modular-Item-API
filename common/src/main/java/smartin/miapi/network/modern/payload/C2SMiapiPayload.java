package smartin.miapi.network.modern.payload;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import smartin.miapi.Miapi;

import java.util.UUID;

public record C2SMiapiPayload(CustomPayload payload) implements CustomPacketPayload {
    public static UUID noPlayerUUID = UUID.fromString("ddfe3f2c-2d4e-4242-8a65-f4641ba9f5f6");

    public static final StreamCodec<FriendlyByteBuf, C2SMiapiPayload> STREAM_CODEC = CustomPacketPayload.codec(C2SMiapiPayload::encode, C2SMiapiPayload::decode);

    public static final Type<C2SMiapiPayload> TYPE = new Type<>(Miapi.id("default-c2s-networking"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static C2SMiapiPayload decode(FriendlyByteBuf friendlyByteBuf) {
        CustomPayload payload = CustomPayload.decode(friendlyByteBuf);
        return new C2SMiapiPayload(payload);
    }

    public void encode(FriendlyByteBuf data) {
        payload().encode(data);
    }

    public static UUID getClientUUID() {
        UUID uuid = C2SMiapiPayload.noPlayerUUID;
        if (Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
            uuid = Minecraft.getInstance().player.getUUID();
        }
        return uuid;
    }
}

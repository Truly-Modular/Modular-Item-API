package smartin.miapi.network.payload;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.network.Networking;
import smartin.miapi.network.modern.payload.CustomPayload;

import java.util.UUID;

public record C2SMiapiOLDPayload(CustomPayload payload) implements CustomPacketPayload {
    public static UUID noPlayerUUID = UUID.fromString("ddfe3f2c-2d4e-4242-8a65-f4641ba9f5f6");

    public static final StreamCodec<FriendlyByteBuf, C2SMiapiOLDPayload> STREAM_CODEC = CustomPacketPayload.codec(C2SMiapiOLDPayload::encode, C2SMiapiOLDPayload::decode);

    public static final Type<C2SMiapiOLDPayload> PACKET_TYPE = new Type<>(Miapi.id("default-c2s-networking-old"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public static C2SMiapiOLDPayload decode(FriendlyByteBuf friendlyByteBuf) {
        CustomPayload payload = CustomPayload.decode(friendlyByteBuf);
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeBytes(payload.data());
        Networking.C2SPackets.get(payload.id()).accept(buf, getPlayer(payload.serverPlayer()));
        return new C2SMiapiOLDPayload(payload);
    }

    public static @Nullable ServerPlayer getPlayer(@NotNull UUID uuid) {
        if (Miapi.server != null) {
            return Miapi.server.getPlayerList().getPlayer(uuid);
        }
        return null;
    }

    public void encode(FriendlyByteBuf data) {
        payload().encode(data);
    }

    public static UUID getClientUUID() {
        UUID uuid = C2SMiapiOLDPayload.noPlayerUUID;
        if (Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
            uuid = Minecraft.getInstance().player.getUUID();
        }
        return uuid;
    }
}

package smartin.miapi.network.modern.payload;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.network.Networking;

import java.util.UUID;

public record C2SMiapiPayload(CustomPayload payload) implements CustomPacketPayload {
    public static UUID noPlayerUUID = UUID.fromString("ddfe3f2c-2d4e-4242-8a65-f4641ba9f5f6");

    public static final StreamCodec<FriendlyByteBuf, C2SMiapiPayload> STREAM_CODEC = CustomPacketPayload.codec(C2SMiapiPayload::encode, C2SMiapiPayload::decode);

    public static final Type<C2SMiapiPayload> PACKET_TYPE = new Type<>(Miapi.id("default-c2s-networking"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public static C2SMiapiPayload decode(FriendlyByteBuf friendlyByteBuf) {
        CustomPayload payload = CustomPayload.decode(friendlyByteBuf);
        if (!Networking.C2SPackets.containsKey(payload.id())) {
            Miapi.LOGGER.error("no reciever for c2s " + payload.id() + " was registered");
        }
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeBytes(payload.data());
        //Networking.C2SPackets.get(payload.id()).accept(buf, getPlayer(payload.serverPlayer()));
        return new C2SMiapiPayload(payload);
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
        UUID uuid = C2SMiapiPayload.noPlayerUUID;
        if (Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
            uuid = Minecraft.getInstance().player.getUUID();
        }
        return uuid;
    }
}

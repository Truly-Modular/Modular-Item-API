package smartin.miapi.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;

import java.util.UUID;

public record CustomDataPayload(CustomDataData data) implements CustomPacketPayload {
    public static UUID noPlayerUUID = UUID.fromString("ddfe3f2c-2d4e-4242-8a65-f4641ba9f5f6");

    public static final StreamCodec<FriendlyByteBuf, CustomDataPayload> STREAM_CODEC = CustomPacketPayload.codec(CustomDataPayload::write, CustomDataPayload::new);

    public static final CustomPacketPayload.Type<CustomDataPayload> TYPE = new Type<>(Miapi.id("default-common-networking"));

    public CustomDataPayload(FriendlyByteBuf friendlyByteBuf) {
        this(new CustomDataData(friendlyByteBuf.readUtf(), getPlayer(friendlyByteBuf.readUUID()), friendlyByteBuf));
        Networking.implementation.trigger(data().id(), data().data(), data().serverPlayer());
    }

    public static @Nullable ServerPlayer getPlayer(@NotNull UUID uuid) {
        if(Miapi.server!=null){
            return Miapi.server.getPlayerList().getPlayer(uuid);
        }
        return null;
    }

    public void write(FriendlyByteBuf data) {
        data.writeUtf(data().id());
        UUID playerUUID = data().serverPlayer() == null ? noPlayerUUID : data().serverPlayer().getUUID();
        data.writeUUID(playerUUID);
        data.writeBytes(data().data());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record CustomDataData(String id, @Nullable ServerPlayer serverPlayer, FriendlyByteBuf data) {

    }
}

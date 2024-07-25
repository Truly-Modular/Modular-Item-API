package smartin.miapi.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;

import java.util.UUID;

public record CustomDataPayload(CustomDataData data) implements CustomPacketPayload {
    public static UUID noPlayerUUID = UUID.fromString("ddfe3f2c-2d4e-4242-8a65-f4641ba9f5f6");

    public static final StreamCodec<FriendlyByteBuf, CustomDataPayload> STREAM_CODEC = CustomPacketPayload.codec(CustomDataPayload::write, CustomDataPayload::decode);

    public static final CustomPacketPayload.Type<CustomDataPayload> TYPE = new Type<>(Miapi.id("default-common-networking"));

    public CustomDataPayload(FriendlyByteBuf friendlyByteBuf) {
        this(new CustomDataData(
                friendlyByteBuf.readUtf(),
                getPlayer(friendlyByteBuf.readUUID()),
                friendlyByteBuf.readByteArray(friendlyByteBuf.readInt())));
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeBytes(data().data());
        Networking.implementation.trigger(data().id(), buf, data().serverPlayer());
    }

    public static CustomDataPayload decode(FriendlyByteBuf friendlyByteBuf) {
        String id = friendlyByteBuf.readUtf();
        Player player = getPlayer(friendlyByteBuf.readUUID());
        int bufferSize = friendlyByteBuf.readInt();
        byte[] buffer = friendlyByteBuf.readByteArray(bufferSize);
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeBytes(buffer);
        ServerPlayer serverPlayer = null;
        if (player instanceof ServerPlayer s) {
            serverPlayer = s;
        }
        //Networking.implementation.trigger(id, buf, serverPlayer);
        return new CustomDataPayload(new CustomDataData(id, serverPlayer, buffer));
    }

    public static @Nullable ServerPlayer getPlayer(@NotNull UUID uuid) {
        if (Miapi.server != null) {
            return Miapi.server.getPlayerList().getPlayer(uuid);
        }
        return null;
    }

    public void write(FriendlyByteBuf data) {

        data.writeUtf(data().id());
        UUID playerUUID = data().serverPlayer() == null ? noPlayerUUID : data().serverPlayer().getUUID();
        data.writeUUID(playerUUID);
        byte[] bytes = data().data();
        data.writeInt(bytes.length);
        data.writeByteArray(bytes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record CustomDataData(String id, @Nullable ServerPlayer serverPlayer, byte[] data) {

    }
}

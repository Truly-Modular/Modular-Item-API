package smartin.miapi.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.network.Networking;
import smartin.miapi.network.modern.payload.CustomPayload;

import java.util.UUID;

public record CustomDataPayload(CustomPayload data) implements CustomPacketPayload {
    public static UUID noPlayerUUID = UUID.fromString("ddfe3f2c-2d4e-4242-8a65-f4641ba9f5f6");

    public static final StreamCodec<FriendlyByteBuf, CustomDataPayload> STREAM_CODEC = CustomPacketPayload.codec(CustomDataPayload::write, CustomDataPayload::decode);

    public static final CustomPacketPayload.Type<CustomDataPayload> TYPE = new Type<>(Miapi.id("default-common-networking"));

    public CustomDataPayload(FriendlyByteBuf friendlyByteBuf) {
        this(new CustomPayload(
                friendlyByteBuf.readUtf(),
                friendlyByteBuf.readUUID(),
                friendlyByteBuf.readByteArray(friendlyByteBuf.readInt())));
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeBytes(data().data());
    }

    public static CustomDataPayload decode(FriendlyByteBuf friendlyByteBuf) {
        boolean isClient = friendlyByteBuf.readBoolean();
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
        Miapi.LOGGER.info("recieved packet " + id + " on thread " + Thread.currentThread().getName() + " isClient " + isClient);
        try {
            if (isClient) {
                if(!Networking.S2CPackets.containsKey(id)){
                    Miapi.LOGGER.error("no reciever for s2c "+id+" was registered");
                }
                Networking.S2CPackets.get(id).accept(buf);
            } else {
                if(!Networking.C2SPackets.containsKey(id)){
                    Miapi.LOGGER.error("no reciever for c2s "+id+" was registered");
                }
                Networking.C2SPackets.get(id).accept(buf, serverPlayer);
            }
        } catch (RuntimeException exception) {
            Miapi.LOGGER.error("Networking issue: ", exception);
        }
        return new CustomDataPayload(new CustomPayload(id, null, buffer));
    }

    public static @Nullable ServerPlayer getPlayer(@NotNull UUID uuid) {
        if (Miapi.server != null) {
            return Miapi.server.getPlayerList().getPlayer(uuid);
        }
        return null;
    }

    public void write(FriendlyByteBuf data) {
        data.writeBoolean(Environment.isClient());
        data.writeUtf(data().id());
        UUID playerUUID = data().serverPlayer();
        if (playerUUID == null) {
            Miapi.LOGGER.warn("player is null? this is not normal");
            playerUUID = noPlayerUUID;
        }
        data.writeUUID(playerUUID);
        byte[] bytes = data().data();
        data.writeInt(bytes.length);
        Miapi.LOGGER.info("sending packet " + data().id() + " on thread " + Thread.currentThread().getName());
        data.writeByteArray(bytes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}

package smartin.miapi.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.network.modern.ModernNetworking;
import smartin.miapi.network.modern.payload.CustomPayload;
import smartin.miapi.network.modern.payload.S2CMiapiPayload;
import smartin.miapi.network.payload.C2SMiapiOLDPayload;
import smartin.miapi.network.payload.S2CMiapiOLDPayload;

import java.util.UUID;

public class NetworkingImplCommon extends NetworkingImpl {
    protected NetworkingImplCommon instance;

    public NetworkingImplCommon() {
        instance = this;
        try {
            if (Environment.isClient()) {
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2CMiapiOLDPayload.TYPE, S2CMiapiOLDPayload.STREAM_CODEC, (packet, context) -> {

                });
            } else {
                NetworkManager.registerS2CPayloadType(S2CMiapiPayload.TYPE, S2CMiapiPayload.STREAM_CODEC);
            }
            NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2SMiapiOLDPayload.PACKET_TYPE, C2SMiapiOLDPayload.STREAM_CODEC, (packet, context) -> {

            });
        } catch (RuntimeException e) {
            Miapi.LOGGER.error("could not setup networking", e);
        }
        ModernNetworking.setup();
    }

    public void setupServer() {
        //NetworkManager.registerS2CPayloadType(CustomDataPayload.TYPE, CustomDataPayload.STREAM_CODEC, List.of());
        //NetworkManager.registerReceiver(NetworkManager.Side.C2S, CustomDataPayload.TYPE, CustomDataPayload.STREAM_CODEC, (value, context) -> {

        //});
    }


    public void sendPacketToServer(String identifier, FriendlyByteBuf buffer) {
        //FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        //buf.writeUtf(identifier);
        //buf.writeBytes(buffer.copy());
        UUID player = Minecraft.getInstance().player.getUUID();
        NetworkManager.sendToServer(new C2SMiapiOLDPayload(new CustomPayload(identifier, player,
                buffer.array())));
    }

    public void sendPacketToClient(String identifier, ServerPlayer player, FriendlyByteBuf buffer) {
        NetworkManager.sendToPlayer(player, new S2CMiapiOLDPayload(new CustomPayload(identifier, player.getUUID(), buffer.array())));
    }

    @Override
    public FriendlyByteBuf createBuffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}

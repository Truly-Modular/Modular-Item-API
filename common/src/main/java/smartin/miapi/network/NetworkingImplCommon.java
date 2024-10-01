package smartin.miapi.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import smartin.miapi.network.modern.ModernNetworking;
import smartin.miapi.network.modern.payload.C2SMiapiPayload;
import smartin.miapi.network.modern.payload.CustomPayload;
import smartin.miapi.network.modern.payload.S2CMiapiPayload;

import java.util.UUID;

public class NetworkingImplCommon extends NetworkingImpl {
    protected NetworkingImplCommon instance;

    public NetworkingImplCommon() {
        instance = this;
        if (Platform.getEnv().equals(EnvType.CLIENT)) {
            //NetworkManager.registerS2CPayloadType(CustomDataPayload.TYPE, CustomDataPayload.STREAM_CODEC, List.of());
        }
        //NetworkManager.registerReceiver(NetworkManager.Side.S2C, CustomDataPayload.TYPE, CustomDataPayload.STREAM_CODEC, (value, context) -> {

        //});
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
        NetworkManager.sendToServer(new C2SMiapiPayload(new CustomPayload(identifier, player,
                buffer.array())));
    }

    public void sendPacketToClient(String identifier, ServerPlayer player, FriendlyByteBuf buffer) {
        NetworkManager.sendToPlayer(player, new S2CMiapiPayload(new CustomPayload(identifier, player.getUUID(), buffer.array())));
    }

    @Override
    public FriendlyByteBuf createBuffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}

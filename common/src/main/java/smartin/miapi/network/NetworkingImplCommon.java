package smartin.miapi.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class NetworkingImplCommon extends NetworkingImpl {
    protected NetworkingImplCommon instance;

    public NetworkingImplCommon() {
        instance = this;
        if (Platform.getEnv().equals(EnvType.CLIENT)) {
            //NetworkManager.registerS2CPayloadType(CustomDataPayload.TYPE, CustomDataPayload.STREAM_CODEC, List.of());
        }
    }

    public void setupServer() {
        NetworkManager.registerS2CPayloadType(CustomDataPayload.TYPE, CustomDataPayload.STREAM_CODEC, List.of());
    }


    public void sendPacketToServer(String identifier, FriendlyByteBuf buffer) {
        //FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        //buf.writeUtf(identifier);
        //buf.writeBytes(buffer.copy());
        NetworkManager.sendToServer(new CustomDataPayload(new CustomDataPayload.CustomDataData(identifier, null, buffer)));
    }


    public void sendPacketToClient(String identifier, ServerPlayer player, FriendlyByteBuf buffer) {
        NetworkManager.sendToPlayer(player, new CustomDataPayload(new CustomDataPayload.CustomDataData(identifier, player, buffer)));
    }

    @Override
    public FriendlyByteBuf createBuffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}

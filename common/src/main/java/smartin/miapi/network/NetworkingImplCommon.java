package smartin.miapi.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import smartin.miapi.Miapi;

public class NetworkingImplCommon extends NetworkingImpl {
    protected ResourceLocation channelIdentifier = ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "defaultchannel");

    public NetworkingImplCommon() {
        if (Platform.getEnv().equals(EnvType.CLIENT)) {
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, channelIdentifier, (buf, context) -> {
                String packetID = buf.readUtf();
                this.trigger(packetID, buf, null);
            });
        }
    }

    public void setupServer() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, channelIdentifier, (buf, context) -> {
            String packetID = buf.readUtf();
            this.trigger(packetID, buf, (ServerPlayer) context.getPlayer());
        });
    }

    @Deprecated
    public void sendPacketToServer(String identifier, FriendlyByteBuf buffer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(identifier);
        buf.writeBytes(buffer.copy());
        NetworkManager.sendToServer(channelIdentifier, buf);
    }

    @Deprecated
    public void sendPacketToClient(String identifier, ServerPlayer player, FriendlyByteBuf buffer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(identifier);
        buf.writeBytes(buffer.copy());
        NetworkManager.sendToPlayer(player, channelIdentifier, buf);
    }

    @Override
    public FriendlyByteBuf createBuffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}

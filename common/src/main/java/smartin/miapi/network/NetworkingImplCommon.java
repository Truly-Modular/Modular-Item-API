package smartin.miapi.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;

public class NetworkingImplCommon extends NetworkingImpl {
    protected Identifier channelIdentifier = new Identifier(Miapi.MOD_ID, "defaultchannel");

    public NetworkingImplCommon() {
        if (Platform.getEnv().equals(EnvType.CLIENT)) {
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, channelIdentifier, (buf, context) -> {
                String packetID = buf.readString();
                this.trigger(packetID, buf, null);
            });
        }
    }

    public void setupServer() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, channelIdentifier, (buf, context) -> {
            String packetID = buf.readString();
            this.trigger(packetID, buf, (ServerPlayerEntity) context.getPlayer());
        });
    }

    public void sendPacketToServer(String identifier, PacketByteBuf buffer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(identifier);
        buf.writeBytes(buffer.copy());
        NetworkManager.sendToServer(channelIdentifier, buf);
    }

    public void sendPacketToClient(String identifier, ServerPlayerEntity player, PacketByteBuf buffer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(identifier);
        buf.writeBytes(buffer.copy());
        NetworkManager.sendToPlayer(player, channelIdentifier, buf);
    }

    @Override
    public PacketByteBuf createBuffer() {
        return new PacketByteBuf(Unpooled.buffer());
    }
}

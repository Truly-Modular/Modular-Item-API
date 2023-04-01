package smartin.miapi.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.network.NetworkingImpl;

public class NetworkingImplFabric extends NetworkingImpl {
    protected Identifier channelIdentifier = new Identifier(Miapi.MOD_ID+"defaultchannel");

    public NetworkingImplFabric(){
        if(Environment.isClient()){
            ClientPlayNetworking.registerGlobalReceiver(channelIdentifier, (client, handler, buf, responseSender) -> {
                String packetID = buf.readString();
                this.trigger(packetID, buf,null);
            });
        }
        ServerPlayNetworking.registerGlobalReceiver(channelIdentifier, (server,player, handler, buf, responseSender) -> {
            String packetID = buf.readString();
            this.trigger(packetID, buf, player);
        });
    }

    public void sendPacketToServer(String identifier, PacketByteBuf buffer){
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(identifier);
        buf.writeBytes(buffer.copy());
        ClientPlayNetworking.send(channelIdentifier,buf);
    }

    public void sendPacketToClient(String identifier, ServerPlayerEntity player, PacketByteBuf buffer){
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeString(identifier);
        buf.writeBytes(buffer.copy());
        ServerPlayNetworking.send(player,channelIdentifier,buf);
    }

    @Override
    public PacketByteBuf createBuffer() {
        return PacketByteBufs.create();
    }
}

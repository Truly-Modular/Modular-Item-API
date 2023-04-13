package smartin.miapi.network;


import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import smartin.miapi.Miapi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class Networking {
    protected static Map<String,Consumer<PacketByteBuf>> S2CPackets = new HashMap<>();
    protected static Map<String,BiConsumer<PacketByteBuf,ServerPlayerEntity>> C2SPackets = new HashMap<>();
    protected static NetworkingImpl implementation;
    protected static NetworkingImpl.EventListener listener = (key,data, entity)->{
        if(entity==null){
            var consumer = S2CPackets.get(key);
            if(consumer!=null){
                Miapi.LOGGER.error(key);
                consumer.accept(data);
            }
        }
        else{
            var consumer = C2SPackets.get(key);
            if(consumer!=null){
                consumer.accept(data,entity);
            }
        }
    };

    /*
    This classes facility networking over multiple minecraft and loader versions
    The actual networking implementation lays in the NetworkImpl class,
    it only provides a static way to do networking regardless of anything
     */

    public static void setImplementation(NetworkingImpl implementation){
        if(Networking.implementation!=null){
            implementation.unsubscribe(listener);
        }
        Networking.implementation = implementation;
        implementation.subscribe(listener);
    }

    public static PacketByteBuf createBuffer(){
        return implementation.createBuffer();
    }

    public static void registerC2SPacket(String identifier, BiConsumer<PacketByteBuf,ServerPlayerEntity> callback){
        if(C2SPackets.get(identifier)!=null){
            Miapi.LOGGER.error("packet already exists with identifier "+identifier);
        }
        C2SPackets.put(identifier,callback);
    }

    public static void unRegisterC2SPacket(String identifier){
        C2SPackets.remove(identifier);
    }

    public static void unRegisterC2CPacket(String identifier){
        S2CPackets.remove(identifier);
    }

    public static void registerS2CPacket(String identifier, Consumer<PacketByteBuf> callbacks){
        /*
        Should also include a player reference to know who send it, maybe not use consumers?
         */
        if(S2CPackets.get(identifier)!=null){
            Miapi.LOGGER.error("packet already exists with identifier "+identifier);
        }
        S2CPackets.put(identifier,callbacks);
    }

    public static void sendC2S(String packetIdentifier, PacketByteBuf buffer){
        implementation.sendPacketToServer(packetIdentifier,buffer);
    }

    public static void sendS2C(String packetIdentifier, ServerPlayerEntity player, PacketByteBuf buffer){
        implementation.sendPacketToClient(packetIdentifier,player,buffer);
    }
    public static void sendS2C(String packetIdentifier, PacketByteBuf buffer){
        if(Miapi.server==null){
            Miapi.LOGGER.error("cant send packets before Server has fully started");
        }
        Miapi.server.getPlayerManager().getPlayerList().forEach(serverPlayer->{
            PacketByteBuf buf = new PacketByteBuf(buffer.copy());
            implementation.sendPacketToClient(packetIdentifier,serverPlayer,buf);
        });
    }
}
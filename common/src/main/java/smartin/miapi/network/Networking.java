package smartin.miapi.network;


import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import smartin.miapi.Miapi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The Networking class is responsible for managing packets in Minecraft networking.
 * It provides methods to register and unregister packets, create and send packets to clients or servers,
 * and to set the implementation for packet handling.
 * The class uses two maps to manage packets, one for packets sent from the server to the client (S2CPackets)
 * and one for packets sent from the client to the server (C2SPackets).
 * Additionally, the class contains a listener that subscribes to events from the implementation and forwards
 * them to the appropriate packet consumer.
 */
public class Networking {
    /**
     * A map containing packets sent from the server to the client, with the packet identifier as the key
     * and the consumer as the value.
     */
    protected static Map<String, Consumer<PacketByteBuf>> S2CPackets = new HashMap<>();
    /**
     * A map containing packets sent from the client to the server, with the packet identifier as the key
     * and the consumer as the value.
     */
    protected static Map<String, BiConsumer<PacketByteBuf, ServerPlayerEntity>> C2SPackets = new HashMap<>();
    /**
     * The implementation used for packet handling.
     */
    protected static NetworkingImpl implementation;
    /**
     * The listener for events from the implementation. Forwards events to the appropriate packet consumer.
     */
    protected static NetworkingImpl.EventListener listener = (key, data, entity) -> {
        if (entity == null) {
            var consumer = S2CPackets.get(key);
            if (consumer != null) {
                consumer.accept(data);
            }
        } else {
            var consumer = C2SPackets.get(key);
            if (consumer != null) {
                consumer.accept(data, entity);
            }
        }
    };

    /**
     * Empty constructor to prevent instantiation of Networking class.
     */
    protected Networking() {

    }

    /**
     * Sets the implementation for packet handling. If an implementation was already set, unsubscribes
     * the listener from it.
     *
     * @param implementation the implementation for packet handling
     */
    public static void setImplementation(NetworkingImpl implementation) {
        if (Networking.implementation != null) {
            implementation.unsubscribe(listener);
        }
        Networking.implementation = implementation;
        implementation.subscribe(listener);
    }

    /**
     * Creates a new PacketByteBuf instance using the implementation.
     *
     * @return a new PacketByteBuf instance
     */
    public static PacketByteBuf createBuffer() {
        return implementation.createBuffer();
    }

    /**
     * Registers a packet sent from the client to the server with a given identifier and callback.
     *
     * @param identifier the identifier for the packet
     * @param callback   the callback to execute when the packet is received
     */
    public static void registerC2SPacket(String identifier, BiConsumer<PacketByteBuf, ServerPlayerEntity> callback) {
        if (C2SPackets.get(identifier) != null) {
            Miapi.LOGGER.error("packet already exists with identifier " + identifier);
        }
        C2SPackets.put(identifier, callback);
    }

    /**
     * Unregisters a packet sent from the client to the server with a given identifier.
     *
     * @param identifier the identifier for the packet to unregister
     */
    public static void unRegisterC2SPacket(String identifier) {
        C2SPackets.remove(identifier);
    }

    /**
     * unRegisterC2SPacket
     * unRegisterC2CPacket
     * Removes the C2C packet with the specified identifier from the S2CPackets map.
     *
     * @param identifier the identifier of the C2C packet to unregister.
     */
    public static void unRegisterS2CPacket(String identifier) {
        S2CPackets.remove(identifier);
    }

    /**
     * Registers a new S2C packet with the specified identifier and callback function in the S2CPackets map.
     *
     * @param identifier the identifier of the S2C packet to register.
     * @param callbacks  the callback function to execute when the packet is received.
     */
    public static void registerS2CPacket(String identifier, Consumer<PacketByteBuf> callbacks) {
        /*
        Should also include a player reference to know who send it, maybe not use consumers?
         */
        if (S2CPackets.get(identifier) != null) {
            Miapi.LOGGER.error("packet already exists with identifier " + identifier);
        }
        S2CPackets.put(identifier, callbacks);
    }

    /**
     * Sends a C2S packet with the specified identifier and data to the server using the implementation's sendPacketToServer method.
     *
     * @param packetIdentifier the identifier of the packet to send.
     * @param buffer           the data to include in the packet.
     */
    public static void sendC2S(String packetIdentifier, PacketByteBuf buffer) {
        implementation.sendPacketToServer(packetIdentifier, buffer);
    }

    /**
     * Sends an S2C packet with the specified identifier, player, and data to the client using the implementation's sendPacketToClient method.
     *
     * @param packetIdentifier the identifier of the packet to send.
     * @param player           the player to send the packet to.
     * @param buffer           the data to include in the packet.
     */
    public static void sendS2C(String packetIdentifier, ServerPlayerEntity player, PacketByteBuf buffer) {
        implementation.sendPacketToClient(packetIdentifier, player, buffer);
    }

    /**
     * Sends an S2C packet with the specified identifier and data to all connected clients using the implementation's sendPacketToClient method.
     *
     * @param packetIdentifier the identifier of the packet to send.
     * @param buffer           the data to include in the packet.
     */
    public static void sendS2C(String packetIdentifier, PacketByteBuf buffer) {
        if (Miapi.server == null) {
            Miapi.LOGGER.error("cant send packets before Server has fully started");
        }
        Miapi.server.getPlayerManager().getPlayerList().forEach(serverPlayer -> {
            PacketByteBuf buf = new PacketByteBuf(buffer.copy());
            implementation.sendPacketToClient(packetIdentifier, serverPlayer, buf);
        });
    }
}
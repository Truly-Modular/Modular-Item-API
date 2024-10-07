package smartin.miapi.network.modern;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.network.Networking;
import smartin.miapi.network.modern.payload.C2SMiapiPayload;
import smartin.miapi.network.modern.payload.CustomPayload;
import smartin.miapi.network.modern.payload.S2CMiapiPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static smartin.miapi.network.modern.payload.C2SMiapiPayload.getClientUUID;

public class ModernNetworking {
    public static final Map<ResourceLocation, Receiver<?>> s2cReceivers = new HashMap<>();
    public static final Map<ResourceLocation, Receiver<?>> c2sReceivers = new HashMap<>();

    public static void setup() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2CMiapiPayload.TYPE, S2CMiapiPayload.STREAM_CODEC, (packet, context) -> {
            ModernNetworking.s2cReceivers.computeIfPresent(packet.payload().parseId(), ((location, receiver) -> {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Networking.createBuffer(), context.registryAccess());
                buf.writeBytes(packet.payload().data());
                receiver.receive(buf);
                return receiver;
            }));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2SMiapiPayload.TYPE, C2SMiapiPayload.STREAM_CODEC, (packet, context) -> {
            ModernNetworking.c2sReceivers.computeIfPresent(packet.payload().parseId(), ((location, receiver) -> {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Networking.createBuffer(), context.registryAccess());
                buf.writeBytes(packet.payload().data());
                receiver.receive(buf);
                return receiver;
            }));
        });
    }

    public static <T> void registerS2CReceiver(ResourceLocation location, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, RegistryAccess> onReceive) {
        s2cReceivers.put(location, new Receiver<>(codec, onReceive));
    }

    public static <T> void registerC2SReceiver(ResourceLocation location, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, RegistryAccess> onReceive) {
        c2sReceivers.put(location, new Receiver<>(codec, onReceive));
    }

    public static void deregisterS2CReceiver(ResourceLocation location) {
        s2cReceivers.remove(location);
    }

    public static void deregisterC2SReceiver(ResourceLocation location) {
        c2sReceivers.remove(location);
    }

    public static <T> void sendToServer(ResourceLocation location, StreamCodec<RegistryFriendlyByteBuf, T> codec, T data, RegistryAccess access) {
        Receiver<?> receiver = s2cReceivers.get(location);
        if (receiver != null) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Networking.createBuffer(), access);
            codec.encode(buf, data);
            CustomPayload data1 = new CustomPayload(
                    location.toString(),
                    getClientUUID(),
                    buf.array());
            NetworkManager.sendToServer(new C2SMiapiPayload(data1));
        }
    }

    public static <T> void sendToPlayer(ResourceLocation location, Player player, StreamCodec<RegistryFriendlyByteBuf, T> codec, T data) {
        Receiver<?> receiver = c2sReceivers.get(location);
        if (receiver != null) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Networking.createBuffer(), player.registryAccess());
            codec.encode(buf, data);
            CustomPayload data1 = new CustomPayload(
                    location.toString(),
                    player.getUUID(),
                    buf.array());
            NetworkManager.sendToServer(new S2CMiapiPayload(data1));
        }
    }

    public record Receiver<T>(StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<T, RegistryAccess> onReceive) {
        public void receive(RegistryFriendlyByteBuf raw) {
            onReceive().accept(codec().decode(raw), raw.registryAccess());
        }
    }

}

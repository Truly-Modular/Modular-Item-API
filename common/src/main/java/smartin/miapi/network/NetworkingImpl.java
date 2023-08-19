package smartin.miapi.network;


import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class NetworkingImpl{
    protected static final List<EventListener> listeners = new ArrayList<>();

    protected NetworkingImpl(){
    }

    public abstract void sendPacketToServer(String identifier,PacketByteBuf buffer);

    public abstract void sendPacketToClient(String identifier, ServerPlayerEntity player, PacketByteBuf buffer);

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }

    protected void trigger(String path, PacketByteBuf data, @Nullable ServerPlayerEntity entity) {
        for (EventListener listener : listeners) {
            listener.onEvent(path, data, entity);
        }
    }

    public abstract PacketByteBuf createBuffer();

    public interface EventListener {
        void onEvent(String key, PacketByteBuf data, @Nullable ServerPlayerEntity entity);
    }
}

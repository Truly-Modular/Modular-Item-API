package smartin.miapi.network;


import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public abstract class NetworkingImpl{
    protected static final List<EventListener> listeners = new ArrayList<>();

    protected NetworkingImpl(){
    }

    public abstract void sendPacketToServer(String identifier,FriendlyByteBuf buffer);

    public abstract void sendPacketToClient(String identifier, ServerPlayer player, FriendlyByteBuf buffer);

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }

    protected void trigger(String path, FriendlyByteBuf data, @Nullable ServerPlayer entity) {
        for (EventListener listener : listeners) {
            listener.onEvent(path, data, entity);
        }
    }

    public abstract FriendlyByteBuf createBuffer();

    public interface EventListener {
        void onEvent(String key, FriendlyByteBuf data, @Nullable ServerPlayer entity);
    }
}

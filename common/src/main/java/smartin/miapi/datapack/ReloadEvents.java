package smartin.miapi.datapack;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.network.Networking;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReloadEvents {
    protected static final String reloadPacketId = Miapi.MOD_ID + ":events_reload_s2c";
    protected static final String reloadDataPacketId = Miapi.MOD_ID + ":events_reload_s2c_data";
    public static final Map<String, String> DATA_PACKS = new HashMap<>();
    private static Map<String, String> SERVER_DATA_PACKS = new HashMap<>();
    private static int dataPackSize = Integer.MAX_VALUE;
    public static boolean inReload = false;
    public static Map<String, List<String>> syncedPaths = new HashMap<>();

    public static void registerDataPackPathToSync(@Nonnull String modId, @Nonnull String path) {
        syncedPaths.computeIfAbsent(modId, k -> new ArrayList<>()).add(path);
    }

    /**
     * Start is for clearing Caches and preparing the reload.
     * The dataPacks are currently empty
     */
    public static final ReloadEvent START = new ReloadEvent();
    /**
     * Main is for building Caches back up again, dataPacks are loaded after Start and before MAIN
     */
    public static final ReloadEvent MAIN = new ReloadEvent();
    /**
     * End is for sealing Caches and finishing work up
     */
    public static final ReloadEvent END = new ReloadEvent();

    public static void setup() {
        if (Environment.isClient()) {
            clientSetup();
        }
        Networking.registerC2SPacket(reloadPacketId, ((buf, serverPlayerEntity) -> {
            SERVER_DATA_PACKS.forEach((key, data) -> {
                PacketByteBuf buffer = Networking.createBuffer();
                buffer.writeString(key);
                buffer.writeString(data);
                Networking.sendS2C(reloadDataPacketId, serverPlayerEntity, buffer);
            });
        }));
        ReloadEvents.Data.subscribe((path, data) -> {
            DATA_PACKS.put(path, data);
        });
    }

    public static void triggerReloadOnClient(ServerPlayerEntity entity) {
        SERVER_DATA_PACKS = new HashMap<>(DATA_PACKS);
        PacketByteBuf buf = Networking.createBuffer();
        buf.writeInt(DATA_PACKS.size());
        Networking.sendS2C(reloadPacketId, entity, buf);
    }

    private static void clientSetup() {
        Networking.registerS2CPacket(reloadDataPacketId, (buffer) -> {
            //make this execute on main thread maybe
            String key = buffer.readString();
            String data = buffer.readString();
            ReloadEvents.Data.trigger(key, data);
            if (DATA_PACKS.size() == dataPackSize) {
                ReloadEvents.MAIN.fireEvent(true);
                ReloadEvents.END.fireEvent(true);
                dataPackSize = Integer.MAX_VALUE;
                inReload = false;
            }
        });
        Networking.registerS2CPacket(reloadPacketId, (buffer) -> {
            if (inReload) {
                Miapi.LOGGER.error("Cannot trigger a Reload during another reload");
                return;
            }
            inReload = true;
            dataPackSize = buffer.readInt();
            PacketByteBuf buf = Networking.createBuffer();
            buf.writeBoolean(true);
            DATA_PACKS.clear();
            ReloadEvents.START.fireEvent(true);
            Networking.sendC2S(reloadPacketId, buf);
        });
    }


    public interface EventListener {
        void onEvent(boolean isClient);
    }

    public static class ReloadEvent {
        private final Map<EventListener, Float> mainListeners = new HashMap<>();

        public void subscribe(EventListener listener, float priority) {
            mainListeners.put(listener, priority);
        }

        public void subscribe(EventListener listener) {
            subscribe(listener, 0);
        }

        public void unsubscribe(EventListener listener) {
            mainListeners.remove(listener);
        }

        public void fireEvent(boolean isClient) {
            mainListeners.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue()).forEach(eventListenerFloatEntry -> {
                        eventListenerFloatEntry.getKey().onEvent(isClient);
                    });
        }
    }


    public static class Data {
        protected static final List<EventListener> listeners = new ArrayList<>();

        /*
        This event Triggers by default once on server start on the server, and once per clientConnect on the Client
         */
        public static void subscribe(EventListener listener) {
            listeners.add(listener);
        }

        public static void unsubscribe(EventListener listener) {
            listeners.remove(listener);
        }

        /*
        It is not recommended to manually trigger this event.
        If you would like to have a reload trigger the reload event on the server
         */
        public static void trigger(String path, String data) {
            for (EventListener listener : listeners) {
                listener.onEvent(path, data);
            }
        }

        public interface EventListener {
            void onEvent(String path, String data);
        }
    }
}
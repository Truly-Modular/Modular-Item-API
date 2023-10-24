package smartin.miapi.datapack;

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class that handles event-based reloading of data packs and caches.
 */
public class ReloadEvents {
    /**
     * The size of the data pack.
     */
    private static int dataPackSize = Integer.MAX_VALUE;

    /**
     * A flag indicating whether the class is currently in the process of reloading.
     */
    public static boolean inReload = false;

    /**
     * A map that stores the data packs to be synced on the server.
     */
    private static Map<String, String> SERVER_DATA_PACKS = new HashMap<>();

    /**
     * The packet ID for triggering a server-to-client reload.
     */
    protected static final String RELOAD_PACKET_ID = Miapi.MOD_ID + ":events_reload_s2c";

    /**
     * The packet ID for sending data during a server-to-client reload.
     */
    protected static final String RELOAD_DATA_PACKET_ID = Miapi.MOD_ID + ":events_reload_s2c_data";

    /**
     * A map that stores the paths of data packs to be synced.
     */
    public static final Map<String, String> DATA_PACKS = new ConcurrentHashMap<>();

    /**
     * Reloading Datapack.
     */
    public static final Map<String, String> RELOADING_DATA_PACKS = new ConcurrentHashMap<>();

    /**
     * A map that stores the paths of data packs that have been synced.
     */
    public static Map<String, List<String>> syncedPaths = new HashMap<>();

    /**
     * Registers the path of a data pack to be synced.
     *
     * @param modId The ID of the mod.
     * @param path  The path of the data pack to be synced.
     */
    public static void registerDataPackPathToSync(String modId, String path) {
        syncedPaths.computeIfAbsent(modId, k -> new ArrayList<>()).add(path);
    }

    /**
     * The reload event that clears the caches and prepares the reload. The data packs are currently empty.
     */
    public static final ReloadEvent START = new ReloadEvent();

    /**
     * The reload event that builds the caches back up again. The data packs are loaded after Start and before MAIN.
     */
    public static final ReloadEvent MAIN = new ReloadEvent();

    /**
     * The reload event that seals the caches and finishes the work.
     */
    public static final ReloadEvent END = new ReloadEvent();

    /**
     * Sets up the class by registering the server-to-client reload packet and subscribing to the data pack loader.
     */

    /**
     * This int counts the reloads, on reload start it gets increased, on reload end it decreases. if its 0 no reload is happening
     */
    private static int reloadCounter = 0;

    private static long clientReloadTimeStart = 0;

    public static void setup() {
        if (Environment.isClient()) {
            clientSetup();
        }

        Networking.registerC2SPacket(RELOAD_PACKET_ID, ((buf, serverPlayerEntity) -> {
            boolean allowHandshake = buf.readBoolean();
            if (!allowHandshake) {
                Miapi.LOGGER.warn("Client " + serverPlayerEntity.getUuid() + " rejected reload? this should never happen");
                return;
            }
            HashMap<String, String> staticData = new HashMap<>(SERVER_DATA_PACKS);
            Thread workerThread = new Thread(() -> staticData.forEach((key, data) -> {
                PacketByteBuf buffer = Networking.createBuffer();
                buffer.writeString(key);
                buffer.writeString(data);
                Networking.sendS2C(RELOAD_DATA_PACKET_ID, serverPlayerEntity, buffer);
            }));
            workerThread.setName("miapi-handshake-thread");
            workerThread.start();
        }));

        //scedule join?
        PlayerEvent.PLAYER_JOIN.register((ReloadEvents::triggerReloadOnClient));

        START.subscribe(isClient -> {
            reloadCounter++;
        });
        END.subscribe(isClient -> {
            reloadCounter--;
        });

        DataPackLoader.subscribe((dataPack -> {
            synchronized (DATA_PACKS) {
                DATA_PACKS.clear();
                DATA_PACKS.putAll(dataPack);
            }
        }));

    }

    /**
     * Triggers a reload on the client by sending the server-to-client reload packet with the data packs to be synced.
     *
     * @param entity The player entity to send the packet to.
     */
    public static void triggerReloadOnClient(ServerPlayerEntity entity) {
        SERVER_DATA_PACKS = new HashMap<>(DATA_PACKS);
        PacketByteBuf buf = Networking.createBuffer();
        buf.writeInt(DATA_PACKS.size());
        Networking.sendS2C(RELOAD_PACKET_ID, entity, buf);
    }

    /**
     * returns true if a reload is ongoing
     */
    public static boolean isInReload() {
        return reloadCounter != 0;
    }

    private static void clientSetup() {
        Map<String, String> dataTemp = new HashMap<>();
        Networking.registerS2CPacket(RELOAD_DATA_PACKET_ID, (buffer) -> {
            //make this previewStack on main thread maybe
            String key = buffer.readString();
            String data = buffer.readString();
            dataTemp.put(key, data);
            if (dataTemp.size() == dataPackSize) {
                synchronized (DATA_PACKS) {
                    DATA_PACKS.clear();
                    DATA_PACKS.putAll(dataTemp);
                    MinecraftClient.getInstance().execute(() -> {
                        DataPackLoader.trigger(new ConcurrentHashMap<>(DATA_PACKS));
                        ReloadEvents.MAIN.fireEvent(true);
                        ReloadEvents.END.fireEvent(true);
                        Miapi.LOGGER.info("Client load took " + (double) (System.nanoTime() - clientReloadTimeStart) / 1000 / 1000 + " ms");
                        dataPackSize = Integer.MAX_VALUE;
                        inReload = false;
                    });
                }
            }
        });
        Networking.registerS2CPacket(RELOAD_PACKET_ID, (buffer) -> {
            if (inReload) {
                Miapi.LOGGER.error("Cannot trigger a Reload during another reload");
                return;
            }
            int counterinit = 0;
            while (MinecraftClient.getInstance().getNetworkHandler() == null) {
                counterinit++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                if (counterinit > 200) {
                    throw new RuntimeException("Miapi waited 2 Minutes and still could not establish a Connection with the server and is unable to force disconnect");
                }
            }
            inReload = true;
            dataPackSize = buffer.readInt();
            clientReloadTimeStart = System.nanoTime();
            PacketByteBuf buf = Networking.createBuffer();
            buf.writeBoolean(true);
            DATA_PACKS.clear();
            dataTemp.clear();
            ReloadEvents.START.fireEvent(true);
            Networking.sendC2S(RELOAD_PACKET_ID, buf);
        });
    }


    /**
     * An interface for listening to reload events. Implementations of this interface can subscribe to reload events
     * using the {@link ReloadEvent} class.
     */
    public interface EventListener {

        /**
         * Called when a reload event occurs.
         *
         * @param isClient a boolean indicating whether the reload event occurred on the client side (true) or the server side (false)
         */
        void onEvent(boolean isClient);
    }


    /**
     * A class for handling reload events. Instances of this class represent specific stages of the reload process, and
     * can be subscribed to using the {@link #subscribe(EventListener)} and {@link #subscribe(EventListener, float)} methods.
     * When a reload event is fired using the {@link #fireEvent(boolean)} method, the registered listeners will be called in
     * order of their priority (with lower-priority listeners being called first).
     */
    public static class ReloadEvent {
        private final Map<EventListener, Float> mainListeners = new HashMap<>();

        /**
         * Subscribes the given listener to this reload event, with the given priority. Listeners with lower priorities will
         * be called first when this event is fired.
         *
         * @param listener the listener to subscribe
         * @param priority the priority of the listener
         */
        public void subscribe(EventListener listener, float priority) {
            mainListeners.put(listener, priority);
        }

        /**
         * Subscribes the given listener to this reload event, with a default priority of 0. Listeners with lower priorities
         * will be called first when this event is fired.
         *
         * @param listener the listener to subscribe
         */
        public void subscribe(EventListener listener) {
            subscribe(listener, 0);
        }

        /**
         * Unsubscribes the given listener from this reload event.
         *
         * @param listener the listener to unsubscribe
         */
        public void unsubscribe(EventListener listener) {
            mainListeners.remove(listener);
        }

        /**
         * Fires this reload event, calling all registered listeners in order of their priority (with lower-priority
         * listeners being called first). The {@code isClient} parameter indicates whether the event is occurring on the
         * client side (true) or the server side (false).
         *
         * @param isClient a boolean indicating whether the event is occurring on the client side (true) or the server side (false)
         */
        public void fireEvent(boolean isClient) {
            mainListeners.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue()).forEach(eventListenerFloatEntry -> {
                        try {
                            eventListenerFloatEntry.getKey().onEvent(isClient);
                        } catch (Exception e) {
                            Miapi.LOGGER.error("Exception during reload", e);
                        }
                    });
        }
    }

    /**
     * The DataPackLoader class is responsible for loading and managing datapacks.
     * It allows for event listeners to be registered and notified when a datapack is reloaded.
     */
    public static class DataPackLoader {
        /**
         * The list of event listeners that are registered to receive reload events.
         */
        protected static final List<EventListener> listeners = new ArrayList<>();

        /**
         * Adds an event listener to the list of listeners that will be notified when a datapack is reloaded.
         *
         * @param listener the event listener to add
         */
        public static void subscribe(EventListener listener) {
            listeners.add(listener);
        }

        /**
         * Removes an event listener from the list of listeners that will be notified when a datapack is reloaded.
         *
         * @param listener the event listener to remove
         */
        public static void unsubscribe(EventListener listener) {
            listeners.remove(listener);
        }

        /**
         * Notifies all registered event listeners that a datapack has been reloaded.
         * This method is not intended to be called manually, and is called automatically by the system.
         *
         * @param dataPack the datapack in a <Path,Data> map
         */
        public static void trigger(Map<String, String> dataPack) {
            for (EventListener listener : listeners) {
                try {
                    listener.onEvent(dataPack);
                } catch (Exception e) {
                    Miapi.LOGGER.error("Exception during reload", e);
                }
            }
        }

        /**
         * The interface for event listeners that will be notified when a datapack is reloaded.
         */
        public interface EventListener {
            /**
             * Called when a datapack is reloaded.
             *
             * @param dataPack the datapack in a <Path,Data> map
             */
            void onEvent(Map<String, String> dataPack);
        }
    }
}
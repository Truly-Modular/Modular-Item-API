package smartin.miapi.datapack;

import com.mojang.serialization.Codec;
import dev.architectury.event.events.common.PlayerEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.sync.DataSyncer;
import smartin.miapi.datapack.sync.StreamCodecSyncer;
import smartin.miapi.modules.cache.CacheCommands;
import smartin.miapi.network.Networking;
import smartin.miapi.registries.MiapiRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A utility class that handles event-based reloading of data packs and caches.
 */
public class ReloadEvents {
    /**
     * Mod IDs to scan for internal datapacks
     */
    public static final List<String> MOD_IDS_TO_SCAN = new ArrayList<>(List.of("miapi","tm_arsenal","tm_archery","tm_armory"));
    /**
     * This is to register DataSyncer. This can be used by addons to sync their own data from the server to the client.
     * This class will deal with all the default logic to sync the packet
     */
    public static MiapiRegistry<DataSyncer> DATA_SYNCER_REGISTRY = MiapiRegistry.getInstance(DataSyncer.class);

    private static final List<String> RECEIVED_SYNCER = new ArrayList<>();

    /**
     * Data syncer packets are automatically split into not being larger then this size.
     */
    private static final int MAX_PAYLOAD_SIZE = 1_000_000; // Adjust as needed (in bytes)

    /**
     * The packet ID for triggering a server-to-client reload.
     */
    protected static final String RELOAD_PACKET_ID = Miapi.MOD_ID + ":events_reload_s2c";

    /**
     * A map that stores the paths of data packs to be synced.
     */
    public static final Map<ResourceLocation, String> DATA_PACKS = Collections.synchronizedMap(new LinkedHashMap<>());
    /**
     * A map that stores the paths of data packs to be synced.
     */
    public static final Map<ResourceLocation, String> RAW_DATA_PACKS = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * A map that stores the paths of data packs that have been synced.
     */
    public static Map<String, List<String>> SYNCED_PATHS = new HashMap<>();

    /**
     * Registers the path of a data pack to be synced.
     *
     * @param modId The ID of the mod.
     * @param path  The path of the data pack to be synced.
     */
    public static void registerDataPackPathToSync(String modId, String path) {
        SYNCED_PATHS.computeIfAbsent(modId, k -> new ArrayList<>()).add(path);
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
     * At this point all caches are unsealed and the reload is done.
     */
    public static final ReloadEvent POST = new ReloadEvent();

    /**
     * Sets up the class by registering the server-to-client reload packet and subscribing to the data pack loader.
     */

    /**
     * This int counts the reloads, on reload start it gets increased, on reload end it decreases. if its 0 no reload is happening
     */
    public static volatile int reloadCounter = 0;

    public static void setup() {
        if (Environment.isClient()) {
            clientSetup();
        }

        Networking.registerC2SPacket(RELOAD_PACKET_ID, ((buf, serverPlayerEntity) -> {
            boolean allowHandshake = buf.readBoolean();
            boolean reloadServer = buf.readBoolean();
            if (!allowHandshake) {
                Miapi.LOGGER.warn("Client " + serverPlayerEntity.getUUID() + " rejected reload? this should never happen!");
                Miapi.server.sendSystemMessage(Component.literal("Client " + serverPlayerEntity.getDisplayName() + " failed to reload."));
            } else {
                if (reloadServer && serverPlayerEntity.hasPermissions(4)) {
                    CacheCommands.triggerServerReload();
                } else {
                    triggerReloadOnClient(serverPlayerEntity);
                }
            }
        }));


        Codec<Map<ResourceLocation, String>> codec = Codec.unboundedMap(ResourceLocation.CODEC, Miapi.CHUNKED_STRING_CODEC);
        StreamCodec<ByteBuf, Map<ResourceLocation, String>> streamCodec = ByteBufCodecs.fromCodecTrusted(codec);

        DATA_SYNCER_REGISTRY.register(Miapi.id("data_packs"), new StreamCodecSyncer<Map<ResourceLocation, String>>(streamCodec) {
            @Override
            public Map<ResourceLocation, String> getDataServer() {
                Map<ResourceLocation, String> toSend;
                synchronized (DATA_PACKS) {
                    toSend = new LinkedHashMap<>(DATA_PACKS);
                }
                return toSend;
            }

            @Override
            public void interpretData(Map<ResourceLocation, String> data) {
                Minecraft.getInstance().execute(() -> {
                    synchronized (DATA_PACKS) {
                        DATA_PACKS.clear();
                        DATA_PACKS.putAll(data);
                    }
                    DataPackLoader.trigger(data);
                });
            }
        });

        //scedule join?
        PlayerEvent.PLAYER_JOIN.register((ReloadEvents::triggerReloadOnClient));


        START.subscribe((isClient, registryAccess, worker) -> {
            reloadCounter++;
        });
        END.subscribe((isClient, registryAccess, worker) -> {
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
     * this functions makes the client request serverdata or directly ask the server for a full reload
     */
    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public static void requestClientSideDataReload(boolean forceServerReload) {
        FriendlyByteBuf buf = Networking.createBuffer();
        buf.writeBoolean(true);
        buf.writeBoolean(forceServerReload);
        Networking.sendC2S(RELOAD_PACKET_ID, buf);
    }

    /**
     * Triggers a reload on the client by sending the server-to-client reload packet with the data packs to be synced.
     *
     * @param entity The player entity to send the packet to.
     */
    public static void triggerReloadOnClient(ServerPlayer entity) {
        DATA_SYNCER_REGISTRY.getFlatMap().forEach((id, syncer) -> {
            try {
                byte[] fullData = syncer.createDataServer().copy().array();
                sendInChunks(entity, id.toString(), fullData);
                //Miapi.DEBUG_LOGGER.info("sending dataSyncer info to client!" + entity.getUUID() + "!" + Thread.currentThread().getName());
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("Datasyncer " + id + " was not able to create Packet with error ", e);
            }
        });
    }

    private static void sendInChunks(ServerPlayer entity, String id, byte[] data) {
        int totalChunks = (int) Math.ceil((double) data.length / MAX_PAYLOAD_SIZE);

        for (int i = 0; i < totalChunks; i++) {
            int start = i * MAX_PAYLOAD_SIZE;
            int end = Math.min(start + MAX_PAYLOAD_SIZE, data.length);
            byte[] chunk = Arrays.copyOfRange(data, start, end);

            FriendlyByteBuf buf = Networking.createBuffer();
            buf.writeUtf(id);
            buf.writeInt(totalChunks);
            buf.writeInt(i);
            buf.writeInt(chunk.length);
            buf.writeBytes(chunk);


            Networking.sendS2C(RELOAD_PACKET_ID, entity, buf);
        }
    }

    /**
     * returns true if a reload is ongoing
     */
    public static boolean isInReload() {
        return reloadCounter != 0;
    }

    private static long clientReloadTimeStart = System.nanoTime();

    private static void clientSetup() {

        final Map<String, List<byte[]>> chunkBuffer = new HashMap<>();
        final Map<String, Integer> expectedChunks = new HashMap<>();

        Networking.registerS2CPacket(RELOAD_PACKET_ID, (buffer) -> {
            String id = buffer.readUtf();
            int totalChunks = buffer.readInt();
            int chunkIndex = buffer.readInt();
            int len = buffer.readInt();

            if (len < 0 || len > MAX_PAYLOAD_SIZE) {
                Miapi.LOGGER.error("MIAPI invalid chunk length {} for {}", len, id);
                return;
            }

            byte[] chunk = new byte[len];
            buffer.readBytes(chunk);


            chunkBuffer.computeIfAbsent(id, k -> new ArrayList<>(Collections.nCopies(totalChunks, null)))
                    .set(chunkIndex, chunk);

            expectedChunks.putIfAbsent(id, totalChunks);

            if (chunkBuffer.get(id).stream().allMatch(Objects::nonNull)) {
                // All chunks received
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                for (byte[] part : chunkBuffer.get(id)) {
                    try {
                        out.write(part);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                byte[] merged = out.toByteArray();

                FriendlyByteBuf reconstructed = new FriendlyByteBuf(Unpooled.wrappedBuffer(merged));

                DATA_SYNCER_REGISTRY.get(id).interpretDataClient(reconstructed);
                RECEIVED_SYNCER.add(id);

                // Cleanup
                chunkBuffer.remove(id);
                expectedChunks.remove(id);
            }

            if (RECEIVED_SYNCER.size() == DATA_SYNCER_REGISTRY.getFlatMap().keySet().size()) {
                RECEIVED_SYNCER.clear();
                chunkBuffer.clear();
                expectedChunks.clear();
                executeReloadClient();
            }
        });
    }

    private static void executeReloadClient() {
        Minecraft.getInstance().execute(() -> {
            clientReloadTimeStart = System.nanoTime();
            reloadCounter++;
            RegistryAccess access;
            if (Minecraft.getInstance().level != null) {
                access = Minecraft.getInstance().level.registryAccess();
            } else {
                access = Minecraft.getInstance().getConnection().registryAccess();
            }
            ReloadEvents.START.fireEvent(true, access);
            ReloadEvents.MAIN.fireEvent(true, access);
            ReloadEvents.END.fireEvent(true, access);
            reloadCounter--;
            ReloadEvents.POST.fireEvent(true, access);
            Miapi.LOGGER.info("Client load took " + (double) (System.nanoTime() - clientReloadTimeStart) / 1000 / 1000 + " ms");
        });
    }


    /**
     * An interface for listening to reload events. Implementations of this interface can subscribe to reload events
     * using the {@link ReloadEvent} class.
     */
    @FunctionalInterface
    public interface EventListener {

        /**
         * Called when a reload event occurs.
         *
         * @param isClient a boolean indicating whether the reload event occurred on the client side (true) or the server side (false)
         * @param worker
         */
        void onEvent(boolean isClient, @Nullable RegistryAccess registryAccess, Consumer<CompletableFuture<?>> worker);
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
         * @param dataPack the datapack in a Path,Data map
         */
        public static void trigger(Map<ResourceLocation, String> dataPack) {
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
             * @param dataPack the datapack in a Path,Data map
             */
            void onEvent(Map<ResourceLocation, String> dataPack);
        }
    }

}
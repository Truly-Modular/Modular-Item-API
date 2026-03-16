package smartin.miapi.datapack;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.concurrent.CompletableFuture;

public class ReloadHandlerBuilder {

    private ReloadEvent event = ReloadEvents.MAIN;
    private final String location;

    private boolean syncToClient = true;
    private float priority = 0;

    private ReloadEvents.EventListener beforeLoop = (b, r, w) -> {
    };
    private ReloadEvents.EventListener afterLoop = (b, r, w) -> {
    };

    private Runnable clear = () -> {
    };
    private ReloadEvents.EventListener listener;

    public static ReloadHandlerBuilder builder(String location) {
        return new ReloadHandlerBuilder(location);
    }

    private ReloadHandlerBuilder(String location) {
        this.location = location;
    }

    public ReloadHandlerBuilder event(ReloadEvent event) {
        this.event = event;
        return this;
    }

    /**
     * lowest priority gets executed first
     */
    public ReloadHandlerBuilder priority(float priority) {
        this.priority = priority;
        return this;
    }

    /**
     * whether the data is send to the client and the execution is replicated on the client
     */
    public ReloadHandlerBuilder syncToClient(boolean sync) {
        this.syncToClient = sync;
        return this;
    }

    /**
     * executes before any data is re-setup.
     * used to clear old data from previous reloads to prevent contamination
     */
    public ReloadHandlerBuilder clear(Runnable clear) {
        this.clear = clear;
        return this;
    }

    /**
     * executes just before main processing, respects priority - can be used to setup stuff for {@link SingleFileHandler}
     */
    public ReloadHandlerBuilder beforeLoop(ReloadEvents.EventListener beforeLoop) {
        this.beforeLoop = beforeLoop;
        return this;
    }

    /**
     * executes just after main processing, respects priority - can be used to finish stuff for {@link SingleFileHandler}
     */
    public ReloadHandlerBuilder afterLoop(ReloadEvents.EventListener afterLoop) {
        this.afterLoop = afterLoop;
        return this;
    }

    public ReloadHandlerBuilder combinedHandler(ReloadEvents.EventListener mainListener) {
        this.listener = mainListener;
        return this;
    }

    public ReloadHandlerBuilder handler(SingleFileHandler handler) {
        handler(handler, true);
        return this;
    }

    public ReloadHandlerBuilder handler(SingleFileHandler handler, boolean multiThreaded) {
        this.listener = (isClient, registryAccess, worker) -> {
            ReloadEvents.DATA_PACKS.forEach((path, data) -> {
                if (multiThreaded) {
                    worker.accept(CompletableFuture.runAsync(() -> handleSingleFile(handler, isClient, registryAccess, path, data)));
                } else {
                    handleSingleFile(handler, isClient, registryAccess, path, data);
                }
            });
        };
        return this;
    }

    private void handleSingleFile(SingleFileHandler handler, boolean isClient, RegistryAccess registryAccess, ResourceLocation path, String data) {
        if (path.getPath().startsWith(location + "/")) {
            try {
                handler.reloadFile(isClient, path, data, registryAccess);
            } catch (RuntimeException e) {
                Miapi.LOGGER.warn("could not load " + path, e);
            }
        }
    }

    public <T> ReloadHandlerBuilder codec(
            Codec<T> codec,
            SingleDecodedFileHandler<T> handler
    ) {
        return this.handler(new CodecOptimisedFileHandler<>(codec, handler, location));
    }

    public void register() {
        if (syncToClient) {
            ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID, location);
        }

        event.subscribe((isClient, registryAccess, worker) -> {
            beforeLoop.onEvent(isClient, registryAccess, worker);
            listener.onEvent(isClient, registryAccess, worker);
            afterLoop.onEvent(isClient, registryAccess, worker);
        }, priority);

        ReloadEvents.START.subscribe((isClient, registryAccess, worker) -> clear.run());
    }

    @FunctionalInterface
    public interface SingleFileHandler {
        void reloadFile(boolean isClient, ResourceLocation path, String data, RegistryAccess registryAccess);
    }

    @FunctionalInterface
    public interface SingleDecodedFileHandler<T> {
        void reloadFile(boolean isClient, ResourceLocation path, T data, RegistryAccess registryAccess);
    }

    @FunctionalInterface
    public interface SimpleDecoder<T> {
        T decode(boolean isClient, ResourceLocation path, JsonElement element, RegistryAccess registryAccess) throws DecoderException;
    }

    public record CodecOptimisedFileHandler<T>(
            Codec<T> codec,
            SingleDecodedFileHandler<T> handler,
            String path) implements SingleFileHandler {
        @Override
        public void reloadFile(boolean isClient, ResourceLocation path, String data, RegistryAccess registryAccess) {
            try {
                var result = codec().decode(
                        RegistryOps.create(JsonOpsBooleanPatched.INSTANCE, registryAccess),
                        Miapi.gson.fromJson(data, JsonElement.class));
                handler().reloadFile(isClient, path, result.getOrThrow((s) -> new DecoderException("Could not decode " + path + " " + s)).getFirst(), registryAccess);
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("could not decode " + path + " for full-path " + path + e.getMessage());
                if (MiapiConfig.getServerConfig().other.verboseLogging) {
                    Miapi.LOGGER.error("", e);
                    Miapi.LOGGER.error("raw data :");
                    Miapi.LOGGER.error(data);
                }
            }
        }
    }

    public static class DecodingFileHandler<T> implements SingleFileHandler {

        private final SimpleDecoder<T> decoder;
        private final SingleDecodedFileHandler<T> handler;

        public static <T> DecodingFileHandler<T> from(
                SimpleDecoder<T> decoder,
                SingleDecodedFileHandler<T> handler) {
            return new DecodingFileHandler<T>(decoder, handler);
        }

        private DecodingFileHandler(
                SimpleDecoder<T> decoder,
                SingleDecodedFileHandler<T> handler
        ) {
            this.decoder = decoder;
            this.handler = handler;
        }

        @Override
        public void reloadFile(boolean isClient, ResourceLocation path, String data, RegistryAccess registryAccess) {
            try {
                JsonElement element = Miapi.gson.fromJson(data, JsonElement.class);
                T decoded = decoder.decode(isClient, path, element, registryAccess);
                handler.reloadFile(isClient, path, decoded, registryAccess);
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("could not decode {}", path, e);
            }
        }
    }
}
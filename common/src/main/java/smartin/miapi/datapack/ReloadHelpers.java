package smartin.miapi.datapack;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.registries.MiapiRegistry;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ReloadHelpers {
    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location,
            boolean syncToClient,
            Consumer<Boolean> beforeLoop,
            SingleFileHandler handler,
            float priority) {
        if (syncToClient)
            ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID, location);
        event.subscribe((isClient, registryAccess) -> {
            beforeLoop.accept(isClient);
            ReloadEvents.DATA_PACKS.forEach((path, data) -> {
                if (path.getPath().startsWith(location + "/")) {
                    try {
                        handler.reloadFile(isClient, path, data, registryAccess);
                    } catch (RuntimeException e) {
                        Miapi.LOGGER.warn("could not load " + path, e);
                    }
                }
            });
        }, priority);
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location,
            Consumer<Boolean> beforeLoop,
            SingleFileHandler handler,
            float priority) {
        registerReloadHandler(event, location, true, beforeLoop, handler, priority);
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location, MiapiRegistry<?> toClear,
            SingleFileHandler handler) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, 0f);
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location, MiapiRegistry<?> toClear,
            SingleFileHandler handler,
            float prio) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, prio);
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location,
            Map<?, ?> toClear,
            SingleFileHandler handler) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, 0f);
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location, Map<?, ?> toClear,
            SingleFileHandler handler,
            float prio) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, prio);
    }

    public static <T> void registerReloadHandler(
            String location,
            Map<ResourceLocation, T> registry,
            Codec<T> codec,
            float priority) {
        registerReloadHandler(location, registry::clear, registry::put, codec, priority);
    }

    public static <T> void registerReloadHandler(
            String location,
            MiapiRegistry<T> registry,
            Codec<T> codec,
            float priority) {
        registerReloadHandler(location, registry::clear, registry::register, codec, priority);
    }

    public static <T> void registerReloadHandler(
            String location,
            Runnable clear,
            BiConsumer<ResourceLocation, T> onDecode,
            Codec<T> codec,
            float priority) {
        SingleFileHandler handler = new CodecOptimisedFileHandler<>(codec, (isClient, path, data, registryAccess) -> {
            ResourceLocation shortened = Miapi.id(path.toString().replace(":" + location + "/", ":").replace(".json", ""));
            onDecode.accept(shortened, data);
        }, location);
        registerReloadHandler(ReloadEvents.MAIN, location, true, (a) -> {
        }, handler, priority);
        ReloadEvents.START.subscribe((isClient, registryAccess) -> clear.run());
    }


    @FunctionalInterface
    public interface SingleFileHandler {
        void reloadFile(boolean isClient, ResourceLocation path, String data, RegistryAccess registryAccess);
    }

    public record CodecOptimisedFileHandler<T>(
            Codec<T> codec,
            SingleDecodedFileHandler<T> handler,
            String path) implements SingleFileHandler {
        @Override
        public void reloadFile(boolean isClient, ResourceLocation path, String data, RegistryAccess registryAccess) {
            try {
                var result = codec().decode(
                        RegistryOps.create(JsonOps.INSTANCE, registryAccess),
                        Miapi.gson.fromJson(data, JsonElement.class));
                handler().reloadFile(isClient, path, result.getOrThrow((s) -> new DecoderException("Could not decode " + path + " " + s)).getFirst(), registryAccess);
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("could not decode " + path() + " for full-path " + path, e);
                Miapi.LOGGER.error("raw data :");
                Miapi.LOGGER.error(data);
            }
        }

        @FunctionalInterface
        public interface SingleDecodedFileHandler<T> {
            void reloadFile(boolean isClient, ResourceLocation path, T data, RegistryAccess registryAccess);
        }
    }
}

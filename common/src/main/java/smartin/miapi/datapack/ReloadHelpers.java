package smartin.miapi.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.editor.DocPage;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.material.CodecMaterial;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.composite.material.DatapackComposite;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ItemModuleExtension;
import smartin.miapi.modules.abilities.key.KeyBindManager;
import smartin.miapi.modules.edit_options.CreateItemOption.CreateItemOption;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static smartin.miapi.Miapi.gson;

public class ReloadHelpers {
    /**
     * these need to be registered before most other things
     */
    public static void registerReloadHandlers() {
        ReloadHelpers.registerReloadHandler("miapi/modules",
                RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY,
                ItemModule::loadFromData, -0.5f);
        ReloadHelpers.registerReloadHandler("miapi/module_extensions",
                () -> {
                },
                (isClient, path, data, access) -> ItemModuleExtension.loadModuleExtension(path, data, isClient),
                (isClient, path, data, access) -> data.apply(), -0.4f);
        ReloadHelpers.registerReloadHandler("miapi/synergies",
                SynergyManager::clear,
                (isClient, path, data, access) -> SynergyManager.SYNERGY_CODEC.decode(JsonOps.INSTANCE, data).getOrThrow((string ->
                        new DecoderException("Could not decode Synergy " + path + string)
                )).getFirst(),
                (isClient, path, data, access) -> data.register(), 2);
        ReloadHelpers.registerReloadHandler("miapi/wiki", () -> {
                    DocPage.PAGE_LOOKUP.clear();
                }, ((isClient, path, data, registryAccess) ->
                        DocPage.CODEC.decode(
                                JsonOps.INSTANCE,
                                data).getOrThrow(s -> new DecoderException("could not decode wiki info" + s)).getFirst()),
                ((isClient, path, data, registryAccess) -> {
                    DocPage.setupLookup(data);
                }), 0.0f);
        ReloadHelpers.registerReloadHandler(ReloadEvents.MAIN, "miapi/skins/module", SkinOptions.skins, (isClient, path, data, registryAccess) -> {
            SkinOptions.load(path, data);
        }, 1);
        ReloadHelpers.registerReloadHandler(ReloadEvents.MAIN, "miapi/skins/tab", SkinOptions.tabMap, (isClient, path, data, registryAccess) -> {
            SkinOptions.loadTabData(data);
        }, 1);
        ReloadHelpers.registerReloadHandler(ReloadEvents.END, "miapi/create_options", (isClient -> {
            CreateItemOption.CREATE_ITEM_MIAPI_REGISTRY.clear();
        }), ((isClient, path, data, registryAccess) -> {
            CreateItemOption.CreateItem createItem = Miapi.gson.fromJson(data, CreateItemOption.JsonCreateItem.class);
            if (createItem.getBaseModule() != null && createItem.getItem() != null) {
                CreateItemOption.CREATE_ITEM_MIAPI_REGISTRY.register(path, createItem);
            } else {
                Miapi.LOGGER.error("could not find module or item for create option " + path);
                Miapi.LOGGER.error(data);
            }
        }), 0);
        ReloadHelpers.registerReloadHandler(ReloadEvents.MAIN, "miapi/key_binding", true, (isClient) -> {
        }, (isClient, id, data, registryAccess) -> KeyBindManager.processKeybind(isClient, id, data), 0.0f);
        ReloadHelpers.registerReloadHandler("miapi/data_composite", DatapackComposite.DATA_COMPOSITE_REGISTRY, DatapackComposite.DATA_PACK_CODEC, 0.0f);
        ReloadHelpers.registerReloadHandler(ReloadEvents.MAIN, "miapi/material_extensions", (isClient) -> {
                },
                (isClient, path, data, registryAccess) -> {
                    MaterialProperty.loadMaterialExtention(path, data, registryAccess);
                }, -1.5f);
        ReloadHelpers.registerReloadHandler(
                "miapi/materials",
                () -> MaterialProperty.MATERIAL_REGISTRY.clear(),
                (isClient, id, mat, registryAccess) -> {
                    mat.setID(id);
                    mat.generateConverters(isClient);
                    MaterialProperty.MATERIAL_REGISTRY.register(id, mat);
                },
                CodecMaterial.CODEC,
                -2.0f);
        ReloadHelpers.registerReloadHandler(ReloadEvents.MAIN, "miapi/modular_converter", ItemToModularConverter.regexes, (isClient, path, data, registryAccess) ->

        {
            ItemToModularConverter.setupModularConverter(path, data);
        }, 1);
    }

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
            MiapiRegistry<T> registry,
            SimpleDecoder<T> decoder,
            float prio) {
        ReloadHelpers.registerReloadHandler(location, registry::clear,
                decoder,
                (isClient, path, data, registryAccess) -> registry.register(path, data), prio);
    }

    public static <T> void registerReloadHandler(
            String location,
            Runnable clear,
            SimpleDecoder<T> decoder,
            SingleDecodedFileHandler<T> onDecode,
            float prio) {
        registerReloadHandler(ReloadEvents.MAIN, location, true, (a) -> {
        }, new SingleFileHandler() {
            @Override
            public void reloadFile(boolean isClient, ResourceLocation path, String data, RegistryAccess registryAccess) {
                try {
                    JsonElement element = gson.fromJson(data, JsonObject.class);
                    ResourceLocation shortened = Miapi.id(path.toString().replace(":" + location + "/", ":").replace(".json", ""));
                    T decoded = decoder.decode(isClient, shortened, element, registryAccess);
                    onDecode.reloadFile(isClient, shortened, decoded, registryAccess);
                } catch (RuntimeException e) {
                    Miapi.LOGGER.error("could not decode " + path + " for full-path " + path, e);
                    Miapi.LOGGER.error("raw data :");
                    Miapi.LOGGER.error(data);
                }
            }
        }, prio);
        ReloadEvents.START.subscribe((isClient, registryAccess) -> clear.run());
    }

    public static <T> void registerReloadHandler(
            String location,
            Runnable clear,
            BiConsumer<ResourceLocation, T> onDecode,
            Codec<T> codec,
            float priority) {
        registerReloadHandler(location, clear, (isClient, path, data, registryAccess) -> {
            onDecode.accept(path, data);
        }, codec, priority);
    }

    public static <T> void registerReloadHandler(
            String location,
            Runnable clear,
            SingleDecodedFileHandler<T> onDecode,
            Codec<T> codec,
            float priority) {
        SingleFileHandler handler = new CodecOptimisedFileHandler<>(codec, (isClient, path, data, registryAccess) -> {
            ResourceLocation shortened = Miapi.id(path.toString().replace(":" + location + "/", ":").replace(".json", ""));
            onDecode.reloadFile(isClient, shortened, data, registryAccess);
        }, location);
        registerReloadHandler(ReloadEvents.MAIN, location, true, (a) -> {
        }, handler, priority);
        ReloadEvents.START.subscribe((isClient, registryAccess) -> clear.run());
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
}

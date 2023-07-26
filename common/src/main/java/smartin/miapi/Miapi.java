package smartin.miapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.datapack.ReloadListener;
import smartin.miapi.events.property.ApplicationEvents;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.conditions.*;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.network.Networking;
import smartin.miapi.network.NetworkingImplCommon;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Miapi {
    public static final String MOD_ID = "miapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("ModularItem API");
    public static NetworkingImplCommon networkingImplementation;
    public static MinecraftServer server;
    public static Gson gson = new Gson();

    public static void init() {
        setupNetworking();
        ApplicationEvents.setup();
        RegistryInventory.setup();
        ReloadEvents.setup();
        ItemAbilityManager.setup();
        AttributeRegistry.setup();
        ConditionManager.setup();
        LifecycleEvent.SERVER_BEFORE_START.register(minecraftServer -> {
            server = minecraftServer;
            LOGGER.info("Server before started");
        });
        ReloadListenerRegistry.register(ResourceType.SERVER_DATA, new ReloadListener());
        registerReloadHandler(ReloadEvents.MAIN, "modules", RegistryInventory.modules, (isClient, path, data) -> {
            ItemModule.loadFromData(path, data);
        },-0.5f);
        PropertyResolver.propertyProviderRegistry.register("module", (moduleInstance, oldMap) -> {
            HashMap<ModuleProperty, JsonElement> map = new HashMap<>();
            moduleInstance.module.getProperties().forEach((key, jsonData) -> {
                map.put(RegistryInventory.moduleProperties.get(key), jsonData);
            });
            return map;
        });
        PropertyResolver.propertyProviderRegistry.register("moduleData", (moduleInstance, oldMap) -> {
            HashMap<ModuleProperty, JsonElement> map = new HashMap<>();
            String properties = moduleInstance.moduleData.get("properties");
            if (properties != null) {
                JsonObject moduleJson = gson.fromJson(properties, JsonObject.class);
                if (moduleJson != null) {
                    moduleJson.entrySet().forEach(stringJsonElementEntry -> {
                        ModuleProperty property = RegistryInventory.moduleProperties.get(stringJsonElementEntry.getKey());
                        if (property != null) {
                            map.put(property, stringJsonElementEntry.getValue());
                        }
                    });
                }
            }
            return map;
        });
        ModularItemCache.setSupplier(ItemModule.moduleKey, itemStack -> {
            NbtCompound tag = itemStack.getOrCreateNbt();
            try {
                String modulesString = tag.getString(ItemModule.moduleKey);
                Gson gson = new Gson();
                return gson.fromJson(modulesString, ItemModule.ModuleInstance.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        ModularItemCache.setSupplier(ItemModule.propertyKey, itemStack -> ItemModule.getUnmergedProperties((ItemModule.ModuleInstance) ModularItemCache.get(itemStack, ItemModule.moduleKey)));
        StatResolver.registerResolver("translation", new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ItemModule.ModuleInstance instance) {
                return Double.parseDouble(Text.translatable(data).getString());
            }

            @Override
            public String resolveString(String data, ItemModule.ModuleInstance instance) {
                return Text.translatable(data).getString();
            }
        });
        ModularItemStackConverter.converters.add(new ItemToModularConverter());
        if (Environment.isClient()) {
            MiapiClient.init();
        }
        SynergyManager.setup();
    }

    protected static void setupNetworking() {
        networkingImplementation = new NetworkingImplCommon();
        Networking.setImplementation(networkingImplementation);
        networkingImplementation.setupServer();
    }

    public static void registerReloadHandler(ReloadEvents.ReloadEvent event, String location, boolean syncToClient, Consumer<Boolean> beforeLoop, TriConsumer<Boolean, String, String> handler, float priority) {
        if (syncToClient)
            ReloadEvents.registerDataPackPathToSync(MOD_ID, location);
        event.subscribe(isClient -> {
            beforeLoop.accept(isClient);
            ReloadEvents.DATA_PACKS.forEach((path, data) -> {
                if (path.startsWith(location))
                    handler.accept(isClient, path, data);
            });
        }, priority);
    }

    public static void registerReloadHandler(ReloadEvents.ReloadEvent event, String location, boolean syncToClient, TriConsumer<Boolean, String, String> handler) {
        registerReloadHandler(event, location, syncToClient, bl -> {}, handler, 0f);
    }

    public static void registerReloadHandler(ReloadEvents.ReloadEvent event, String location, TriConsumer<Boolean, String, String> handler) {
        registerReloadHandler(event, location, true, bl -> {}, handler, 0f);
    }

    public static void registerReloadHandler(ReloadEvents.ReloadEvent event, String location, MiapiRegistry<?> toClear, TriConsumer<Boolean, String, String> handler) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, 0f);
    }

    public static void registerReloadHandler(ReloadEvents.ReloadEvent event, String location, MiapiRegistry<?> toClear, TriConsumer<Boolean, String, String> handler, float prio) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, prio);
    }

    public static void registerReloadHandler(ReloadEvents.ReloadEvent event, String location, Map<?, ?> toClear, TriConsumer<Boolean, String, String> handler) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, 0f);
    }

    public static void registerReloadHandler(ReloadEvents.ReloadEvent event, String location, Map<?, ?> toClear, TriConsumer<Boolean, String, String> handler, float prio) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, prio);
    }
}
package smartin.miapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redpxnda.nucleus.registry.NucleusNamespaces;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.craft.stat.StatActorType;
import smartin.miapi.datapack.MiapiReloadListener;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.injectors.PropertySubstitution;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.MiapiPermissions;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.network.NetworkingImplCommon;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Miapi {
    public static final String MOD_ID = "miapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("ModularItem API");
    public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("miapi debug");
    public static NetworkingImplCommon networkingImplementation;
    public static MinecraftServer server;
    public static Gson gson = new Gson();

    public static void init() {
        MiapiConfig.getInstance();
        setupNetworking();
        RegistryInventory.setup();
        ReloadEvents.setup();
        ItemAbilityManager.setup();
        AttributeRegistry.setup();
        ConditionManager.setup();
        StatActorType.setup();
        LifecycleEvent.SERVER_BEFORE_START.register(minecraftServer -> server = minecraftServer);
        ReloadListenerRegistry.register(
                ResourceType.SERVER_DATA,
                new MiapiReloadListener(),
                new Identifier(MOD_ID, "main_reload_listener"),
                List.of(new Identifier("minecraft:tags"), new Identifier("minecraft:recipes")));
        registerReloadHandler(ReloadEvents.MAIN, "modules", RegistryInventory.modules,
                (isClient, path, data) -> ItemModule.loadFromData(path, data), -0.5f);
        registerReloadHandler(ReloadEvents.MAIN, "module_extensions", new ConcurrentHashMap<>(),
                (isClient, path, data) -> ItemModule.loadModuleExtension(path, data), -0.4f);

        registerReloadHandler(ReloadEvents.MAIN, "injectors", bl -> PropertySubstitution.injectorsCount = 0,
                (isClient, path, data) -> {
                    JsonElement element = JsonHelper.deserialize(data);
                    if (element instanceof JsonObject object) {
                        PropertySubstitution.targetSelectionDispatcher.dispatcher()
                                .triggerTargetFrom(object.get("target"), PropertySubstitution.getInjector(object));
                        PropertySubstitution.injectorsCount++;
                    } else {
                        LOGGER.warn(
                                "Found a non JSON object PropertyInjector. PropertyInjectors should be JSON objects.");
                    }
                }, 1f);
        ReloadEvents.END.subscribe(isClient -> {
            Miapi.LOGGER.info("Loaded " + PropertySubstitution.injectorsCount + " Injectors/Property Substitutors");
            Miapi.LOGGER.info("Loaded " + RegistryInventory.modules.getFlatMap().size() + " Modules");
        });
        ReloadEvents.END.subscribe((isClient) -> ModularItemCache.discardCache());
        PlayerEvent.PLAYER_JOIN.register((player -> new Thread(() -> MiapiPermissions.getPerms(player)).start()));
        PropertyResolver.propertyProviderRegistry.register("module", (moduleInstance, oldMap) -> {
            Map<ModuleProperty, JsonElement> map = new ConcurrentHashMap<>();
            moduleInstance.module.getProperties()
                    .forEach((key, jsonData) -> map.put(RegistryInventory.moduleProperties.get(key), jsonData));
            return map;
        });
        PropertyResolver.propertyProviderRegistry.register("moduleData", (moduleInstance, oldMap) -> {
            Map<ModuleProperty, JsonElement> map = new ConcurrentHashMap<>();
            String properties = moduleInstance.moduleData.get("properties");
            if (properties != null) {
                JsonObject moduleJson = gson.fromJson(properties, JsonObject.class);
                if (moduleJson != null) {
                    moduleJson.entrySet().forEach(stringJsonElementEntry -> {
                        ModuleProperty property = RegistryInventory.moduleProperties
                                .get(stringJsonElementEntry.getKey());
                        if (property != null) {
                            map.put(property, stringJsonElementEntry.getValue());
                        }
                    });
                }
            }
            return map;
        });
        ModularItemCache.setSupplier(ItemModule.MODULE_KEY, itemStack -> {
            if (itemStack.getItem() instanceof ModularItem) {
                NbtCompound tag = itemStack.getOrCreateNbt();
                try {
                    String modulesString = tag.getString(ItemModule.MODULE_KEY);
                    return Miapi.gson.fromJson(modulesString, ItemModule.ModuleInstance.class);
                } catch (Exception e) {
                    Miapi.LOGGER.error("could not resolve Modules", e);
                }
            }
            return null;
        });
        ModularItemCache.setSupplier(ItemModule.PROPERTY_KEY,
                itemStack -> ItemModule.getUnmergedProperties(
                        ModularItemCache.get(itemStack, ItemModule.MODULE_KEY, new ItemModule.ModuleInstance(ItemModule.empty))));
        ModularItemStackConverter.converters.add(new ItemToModularConverter());
        if (Environment.isClient()) {
            MiapiClient.init();
        }

        NucleusNamespaces.addAddonNamespace("miapi");
    }

    protected static void setupNetworking() {
        networkingImplementation = new NetworkingImplCommon();
        Networking.setImplementation(networkingImplementation);
        networkingImplementation.setupServer();
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location,
            boolean syncToClient,
            Consumer<Boolean> beforeLoop,
            TriConsumer<Boolean, String, String> handler,
            float priority) {
        if (syncToClient)
            ReloadEvents.registerDataPackPathToSync(MOD_ID, location);
        event.subscribe(isClient -> {
            beforeLoop.accept(isClient);
            ReloadEvents.DATA_PACKS.forEach((path, data) -> {
                if (path.startsWith(location)) {
                    try {
                        handler.accept(isClient, path, data);
                    } catch (Exception e) {
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
            TriConsumer<Boolean, String, String> handler,
            float priority) {
        registerReloadHandler(event, location, true, beforeLoop, handler, priority);
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location, MiapiRegistry<?> toClear,
            TriConsumer<Boolean, String, String> handler) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, 0f);
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location, MiapiRegistry<?> toClear,
            TriConsumer<Boolean, String, String> handler,
            float prio) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, prio);
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location,
            Map<?, ?> toClear,
            TriConsumer<Boolean, String, String> handler) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, 0f);
    }

    public static void registerReloadHandler(
            ReloadEvents.ReloadEvent event,
            String location, Map<?, ?> toClear,
            TriConsumer<Boolean, String, String> handler,
            float prio) {
        registerReloadHandler(event, location, true, bl -> toClear.clear(), handler, prio);
    }
}
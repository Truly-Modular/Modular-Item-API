package smartin.miapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.config.ConfigBuilder;
import com.redpxnda.nucleus.config.ConfigManager;
import com.redpxnda.nucleus.config.ConfigType;
import com.redpxnda.nucleus.registry.NucleusNamespaces;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.craft.stat.StatActorType;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.injectors.PropertySubstitution;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.MiapiPermissions;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.cache.CacheCommands;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.material.MaterialCommand;
import smartin.miapi.modules.material.ComponentMaterial;
import smartin.miapi.modules.material.generated.GeneratedMaterialManager;
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.network.NetworkingImplCommon;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.Collections;
import java.util.LinkedHashMap;
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
    public static Codec<ResourceLocation> ID_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<ResourceLocation, T>> decode(DynamicOps<T> ops, T input) {
            Pair<String, T> result = Codec.STRING.decode(ops, input).getOrThrow();
            return DataResult.success(new Pair<>(Miapi.id(result.getFirst()), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ResourceLocation input, DynamicOps<T> ops, T prefix) {
            return Codec.STRING.encode(input.toString(), ops, prefix);
        }
    };

    public static void init() {
        setupConfigs();
        setupNetworking();
        RegistryInventory.setup();
        ReloadEvents.setup();
        ItemAbilityManager.setup();
        AttributeRegistry.setup();
        ConditionManager.setup();
        StatActorType.setup();
        ComponentMaterial.setup();
        GeneratedMaterialManager.setup();


        LifecycleEvent.SERVER_BEFORE_START.register(minecraftServer -> server = minecraftServer);
        PlayerEvent.PLAYER_JOIN.register((player -> new Thread(() -> MiapiPermissions.getPerms(player)).start()));

        registerReloadHandler(ReloadEvents.MAIN, "modules", RegistryInventory.modules,
                (isClient, path, data) -> ItemModule.loadFromData(path, data, isClient), -0.5f);
        registerReloadHandler(ReloadEvents.MAIN, "module_extensions", Collections.synchronizedMap(new LinkedHashMap<>()),
                (isClient, path, data) -> ItemModule.loadModuleExtension(path, data, isClient), -0.4f);

        registerReloadHandler(ReloadEvents.MAIN, "injectors", bl -> PropertySubstitution.injectorsCount = 0,
                (isClient, path, data) -> {
                    JsonElement element = GsonHelper.parse(data);
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
            RegistryInventory.modules.register(ItemModule.empty.name(), ItemModule.empty);
            RegistryInventory.modules.register(ItemModule.internal.name(), ItemModule.internal);
            Miapi.LOGGER.info("Loaded " + PropertySubstitution.injectorsCount + " Injectors/Property Substitutors");
            Miapi.LOGGER.info("Loaded " + RegistryInventory.modules.getFlatMap().size() + " Modules");
            ModularItemCache.discardCache();
        });
        PropertyResolver.register(ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "module"), (moduleInstance, oldMap) -> {
            return new ConcurrentHashMap<>(moduleInstance.module.properties());
        });
        PropertyResolver.register("module_data", (moduleInstance, oldMap) -> {
            Map<ModuleProperty<?>, Object> map = new ConcurrentHashMap<>();
            String properties = moduleInstance.moduleData.get("properties");
            if (properties != null) {
                JsonObject moduleJson = gson.fromJson(properties, JsonObject.class);
                if (moduleJson != null) {
                    moduleJson.entrySet().forEach(stringJsonElementEntry -> {
                        ModuleProperty property = RegistryInventory.moduleProperties
                                .get(stringJsonElementEntry.getKey());
                        if (property != null) {
                            map.put(property, property.decode(stringJsonElementEntry.getValue()));
                        }
                    });
                }
            }
            return map;
        });
        ModularItemCache.setSupplier(ItemModule.MODULE_KEY, itemStack -> {
            if (itemStack.getItem() instanceof VisualModularItem) {
                try {
                    return ItemModule.getModules(itemStack);
                } catch (Exception e) {
                    Miapi.LOGGER.error("could not resolve Modules", e);
                }
            }
            return null;
        });
        ModularItemStackConverter.converters.add(new ItemToModularConverter());
        if (Environment.isClient()) {
            MiapiClient.init();
        }

        NucleusNamespaces.addAddonNamespace(Miapi.MOD_ID);

        CommandRegistrationEvent.EVENT.register((serverCommandSourceCommandDispatcher, registryAccess, listener) -> {
            MaterialCommand.register(serverCommandSourceCommandDispatcher);
            CacheCommands.register(serverCommandSourceCommandDispatcher);
        });

        LifecycleEvent.SERVER_STARTED.register((minecraftServer -> {
            if (MiapiConfig.INSTANCE.server.other.doubleReload) {
                Miapi.LOGGER.info("Truly Modular will now go onto reload twice.");
                Miapi.LOGGER.info("This is done because for compat reasons and because forge sometimes breaks badly");
                Miapi.LOGGER.info("This can be turned off in Miapis config.json");
                CacheCommands.triggerServerReload();
            }
        }));
    }

    public static ResourceLocation id(String string) {
        String[] parts = string.split(":");
        if (parts.length > 1) {
            return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
        }
        return ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, string);
    }

    public static ResourceLocation id(String namespace, String id) {
        return ResourceLocation.fromNamespaceAndPath(namespace, id);
    }

    protected static void setupNetworking() {
        networkingImplementation = new NetworkingImplCommon();
        Networking.setImplementation(networkingImplementation);
        networkingImplementation.setupServer();
    }

    protected static void setupConfigs() {
        ConfigManager.register(ConfigBuilder.automatic(MiapiConfig.class)
                .id(MOD_ID + ":merged")
                .fileLocation(Miapi.MOD_ID)
                .type(ConfigType.COMMON)
                .creator(MiapiConfig::new)
                .updateListener(c -> {
                    MiapiConfig.INSTANCE = c;
                    if (Environment.isClient()) {
                        GlintProperty.updateConfig();
                    }
                })
                .automaticScreen());
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
                if (path.startsWith(location + "/")) {
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
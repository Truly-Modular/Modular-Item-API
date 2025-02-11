package smartin.miapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.registry.NucleusNamespaces;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.craft.BlueprintManager;
import smartin.miapi.craft.stat.StatActorType;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.material.ComponentMaterial;
import smartin.miapi.material.MaterialCommand;
import smartin.miapi.material.MaterialIcons;
import smartin.miapi.material.generated.GenerateConvertersHelper;
import smartin.miapi.material.generated.GenerateConvertersHelperArmor;
import smartin.miapi.material.generated.GeneratedMaterialManager;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.MiapiPermissions;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.key.KeyBindManager;
import smartin.miapi.modules.abilities.key.MiapiBinding;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.cache.CacheCommands;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.network.NetworkingImplCommon;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The following docs are used for automatic wiki generation
 *
 * @header Wiki for 1.21
 * @description_start Welcome to the Wiki for Truly Modular 1.21
 * This Wiki is structured to help Modpack developers/people who want to add/modify content in Truly Modular via Datapacks
 * It goes in-depth about various Unique things to truly modular, but also over some common Datapack things,
 * It is aimed at all people
 * @description_end
 * @path root
 * @java false
 */
public class Miapi {
    public static final String MOD_ID = "miapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("ModularItem API");
    public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("miapi debug");
    public static NetworkingImplCommon networkingImplementation;
    public static MinecraftServer server;
    public static RegistryAccess registryAccess;
    /**
     * idk, sometimes in networking bools seem to become 0 and 1, default codec cant deal with that,
     * this one can
     */
    public static Codec<Boolean> FIXED_BOOL_CODEC = Codec.withAlternative(
            Codec.BOOL,
            Codec.INT.xmap(i -> i == 1, b -> (b ? 0 : 1)));

    public static Gson gson = new GsonBuilder()
            .create();
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
        CodecBehavior.registerClass(Transform.class, Transform.CODEC);
        CodecBehavior.registerClass(DoubleOperationResolvable.class, DoubleOperationResolvable.CODEC);
        CodecBehavior.registerClass(ModuleInstance.class, ModuleInstance.CODEC);
        CodecBehavior.registerClass(ModuleCondition.class, ConditionManager.CONDITION_CODEC_DIRECT);
        CodecBehavior.registerClass(ResourceLocation.class, ResourceLocation.CODEC);
        CodecBehavior.registerClass(CompoundTag.class, CompoundTag.CODEC);
        CodecBehavior.registerClass(MaterialIcons.SpinSettings.class, MaterialIcons.SpinSettings.CODEC);
        CodecBehavior.registerClass(EquipmentSlotGroup.class, EquipmentSlotGroup.CODEC);
        CodecBehavior.registerClass(EquipmentSlot.class, EquipmentSlot.CODEC);
        if (Environment.isClient()) {
            CodecBehavior.registerClass(MiapiBinding.class, MiapiBinding.CODEC);
        }

        MiapiConfig.setupConfigs();
        setupNetworking();
        RegistryInventory.setup();
        ReloadEvents.setup();
        ItemAbilityManager.setup();
        AttributeRegistry.setup();
        ConditionManager.setup();
        StatActorType.setup();
        ComponentMaterial.setup();
        GeneratedMaterialManager.setup();
        KeyBindManager.setup();

        LifecycleEvent.SERVER_BEFORE_START.register(minecraftServer -> {
            server = minecraftServer;
            registryAccess = minecraftServer.reloadableRegistries().get();
        });
        PlayerEvent.PLAYER_JOIN.register((player -> new Thread(() -> MiapiPermissions.getPerms(player)).start()));

        registerReloadHandler(ReloadEvents.MAIN, "miapi/modules", RegistryInventory.modules,
                (isClient, path, data, access) -> ItemModule.loadFromData(path, data, isClient), -0.5f);
        registerReloadHandler(ReloadEvents.MAIN, "miapi/module_extensions", Collections.synchronizedMap(new LinkedHashMap<>()),
                (isClient, path, data, access) -> ItemModule.loadModuleExtension(path, data, isClient), -0.4f);
        ReloadEvents.END.subscribe((isClient, registryAccess) -> {
            RegistryInventory.modules.register(ItemModule.empty.id(), ItemModule.empty);
            RegistryInventory.modules.register(ItemModule.internal.id(), ItemModule.internal);
            Miapi.LOGGER.info("Loaded " + RegistryInventory.modules.getFlatMap().size() + " Modules");
            ModularItemCache.discardCache();
        });
        PropertyResolver.register(ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "miapi/module"), (moduleInstance, oldMap) -> {
            Map<ModuleProperty<?>, Object> map = moduleInstance.module.properties();
            if (map == null) {
                map = new HashMap<>();
                Miapi.LOGGER.warn("Item Modules has no properties. this is a api issue. please report this. this should not happen");
            }
            return new ConcurrentHashMap<>(map);
        });
        PropertyResolver.register("miapi/module_data", (moduleInstance, oldMap) -> {
            Map<ModuleProperty<?>, Object> map = new ConcurrentHashMap<>(oldMap);
            Map<ModuleProperty<?>, Object> toMerge = new ConcurrentHashMap<>(ModuleDataPropertiesManager.getProperties(moduleInstance));
            map.putAll(toMerge);
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
            if (MiapiConfig.INSTANCE.server.other.doubleReload && false) {
                Miapi.LOGGER.info("Truly Modular will now go onto reload twice.");
                Miapi.LOGGER.info("This is done because for compat reasons and because forge sometimes breaks badly");
                Miapi.LOGGER.info("This can be turned off in Miapi`s config.json");
                CacheCommands.triggerServerReload();
            }
        }));
        BlueprintManager.setup();

        MiapiEvents.GENERATE_MATERIAL_CONVERTERS.register((material, tools, armorItems, isClient) -> {
            try {
                GenerateConvertersHelperArmor.setup(armorItems, material);
                GenerateConvertersHelper.setupTools(tools, material);
            } catch (Exception e) {
                Miapi.LOGGER.warn("failed to setup converters for " + material.getID(), e);
            }
            return EventResult.pass();
        });
    }


    public static String camelToSnake(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        boolean conversionOccurred = false;

        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!result.isEmpty()) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
                conversionOccurred = true;
            } else {
                result.append(c);
            }
        }

        if (conversionOccurred) {
            LOGGER.info("Converted camelCase to snake_case: " + input);
        }

        return result.toString();
    }

    public static ResourceLocation id(String string) {
        string = camelToSnake(string);
        String[] parts = string.split(":");
        if (parts[0].equals("arsenal")) {
            parts[0] = "tm_arsenal";
        }
        if (parts[0].equals("archery")) {
            parts[0] = "tm_archery";
        }
        if (parts[0].equals("armory")) {
            parts[0] = "tm_armory";
        }
        if (parts.length > 1) {
            return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
        }
        return ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, string);
    }

    public static ResourceLocation id(String namespace, String id) {
        return ResourceLocation.fromNamespaceAndPath(namespace, id);
    }

    public static String toLangString(ResourceLocation id) {
        String lang = id.toString();
        lang = lang.replace(":", ".");
        lang = lang.replace("/", ".");
        return lang;
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
            SingleFileHandler handler,
            float priority) {
        if (syncToClient)
            ReloadEvents.registerDataPackPathToSync(MOD_ID, location);
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
            float prio) {
        SingleFileHandler handler = new CodecOptimisedFileHandler<>(codec, (isClient, path, data, registryAccess) -> {
            ResourceLocation shortened = Miapi.id(path.toString().replace(":" + location+"/", ":").replace(".json", ""));
            registry.put(shortened, data);
        }, location, (a) -> {
        });
        registerReloadHandler(ReloadEvents.MAIN, location, true, (a) -> registry.clear(), handler, prio);
    }

    @FunctionalInterface
    public interface SingleFileHandler {
        void reloadFile(boolean isClient, ResourceLocation path, String data, RegistryAccess registryAccess);
    }

    public record CodecOptimisedFileHandler<T>(
            Codec<T> codec,
            SingleDecodedFileHandler<T> handler,
            String path,
            Consumer<Void> toClear) implements SingleFileHandler {
        @Override
        public void reloadFile(boolean isClient, ResourceLocation path, String data, RegistryAccess registryAccess) {
            try {
                var result = codec().decode(
                        RegistryOps.create(JsonOps.INSTANCE, registryAccess),
                        Miapi.gson.fromJson(data, JsonElement.class));
                handler().reloadFile(isClient, path, result.getOrThrow((s) -> new DecoderException("Could not decode " + path + " " + s)).getFirst(), registryAccess);
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("could not decode " + path() + " for path " + path(), e);
            }
        }

        @FunctionalInterface
        public interface SingleDecodedFileHandler<T> {
            void reloadFile(boolean isClient, ResourceLocation path, T data, RegistryAccess registryAccess);
        }
    }
}
package smartin.miapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.registry.NucleusNamespaces;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.blueprint.BlueprintManager;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.craft.stat.StatActorType;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.datapack.ReloadHelpers;
import smartin.miapi.editor.EditorCommands;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.PoseCommands;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.loot.LootHelper;
import smartin.miapi.loot.MaterialSwapLootFunction;
import smartin.miapi.loot.ModuleSwapLootFunction;
import smartin.miapi.material.ComponentMaterial;
import smartin.miapi.material.MaterialCommand;
import smartin.miapi.material.MaterialIcons;
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
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.network.NetworkingImplCommon;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    @SuppressWarnings("unused")
    public static final Logger DEBUG_LOGGER = LoggerFactory.getLogger("miapi debug");
    public static NetworkingImplCommon networkingImplementation;
    public static MinecraftServer server;
    public static RegistryAccess registryAccess;
    public static RegistryAccess clientRegistryAccess;
    /**
     * idk, sometimes in networking booleans seem to become 0 and 1, default codec cant deal with that,
     * this one can
     */
    public static Codec<Boolean> FIXED_BOOL_CODEC = Codec.withAlternative(
            Codec.BOOL,
            Codec.INT.xmap(i -> i == 1, b -> (b ? 0 : 1)));

    public static <T> Codec<List<T>> toListOrSimple(Codec<T> base) {
        return Codec.withAlternative(Codec.list(base), base, List::of);
    }

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

    public static DynamicOps<Tag> BOOL_CORRECTED_OPS = new NbtOps() {
    };

    private static final int CHUNK_SIZE = 9_000; // or whatever limit you want
    public static final Codec<String> CHUNKED_STRING_CODEC = Codec.list(Codec.STRING)
            .xmap(
                    list -> String.join("", list),
                    str -> {
                        List<String> parts = new ArrayList<>();
                        for (int i = 0; i < str.length(); i += CHUNK_SIZE) {
                            parts.add(str.substring(i, Math.min(str.length(), i + CHUNK_SIZE)));
                        }
                        return parts;
                    }
            );

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
        CodecBehavior.registerClass(MaterialSwapLootFunction.class, MaterialSwapLootFunction.CODEC.codec());
        CodecBehavior.registerClass(ModuleSwapLootFunction.class, ModuleSwapLootFunction.CODEC.codec());
        if (Environment.isClient()) {
            CodecBehavior.registerClass(MiapiBinding.class, MiapiBinding.CODEC);
        }


        //ItemStackAccessor.setCODEC(ModuleInstance.registrySavingCodec(ItemStackAccessor.getCODEC(), (i, registryAccess) ->
        //        ModularItemStackConverter.lookupMap.put(i, registryAccess)));

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
        ReloadHelpers.registerReloadHandlers();

        LifecycleEvent.SERVER_BEFORE_START.register(minecraftServer -> {
            server = minecraftServer;
            registryAccess = minecraftServer.reloadableRegistries().get();
        });
        PlayerEvent.PLAYER_JOIN.register((player -> new Thread(() -> MiapiPermissions.getPerms(player)).start()));
        ReloadEvents.END.subscribe((isClient, registryAccess) -> {
            RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.register(ItemModule.empty.id(), ItemModule.empty);
            RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.register(ItemModule.internal.id(), ItemModule.internal);
            Miapi.LOGGER.info("Loaded " + RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.getFlatMap().size() + " Modules");
            MiapiEvents.CLEAR_CACHE.invoker().onReload();
        });
        PropertyResolver.register(ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "miapi/module"), (moduleInstance, oldMap) -> {
            Map<ModuleProperty<?>, Object> map = moduleInstance.getModule().properties();
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
            if (VisualModularItem.isVisualModularItem(itemStack)) {
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
            PoseCommands.register(serverCommandSourceCommandDispatcher);
            EditorCommands.register(serverCommandSourceCommandDispatcher);
        });
        BlueprintManager.setup();
        LootHelper.setup();
        ReloadEvents.POST.subscribe(new ReloadEvents.EventListener() {
            @Override
            public void onEvent(boolean isClient, @Nullable RegistryAccess registryAccess) {
                if (Miapi.server != null) {
                    Miapi.server.getPlayerList().getPlayers().forEach(p -> {
                        p.getInventory().setChanged();
                        CompoundTag tag = new CompoundTag();
                        if (p.save(tag)) {
                            p.load(tag);
                        }
                        Arrays.stream(EquipmentSlot.values()).forEach(equipmentSlot -> {
                            ItemStack stack = p.getItemBySlot(equipmentSlot);
                            if (ModularItem.isModularItem(stack)) {
                                ComponentApplyProperty.updateItemStack(stack, p.registryAccess());
                                p.equipmentHasChanged(p.getItemBySlot(equipmentSlot), p.getItemBySlot(equipmentSlot));
                            }
                        });
                    });
                }
            }
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

    public static <T> MapCodec<T> withAlternative(final MapCodec<T> primary, final MapCodec<? extends T> alternative) {
        return Codec.mapEither(
                primary,
                alternative
        ).xmap(
                Either::unwrap,
                Either::left
        );
    }

}
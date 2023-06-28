package smartin.miapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.gui.crafting.CraftingGUI;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.datapack.ReloadListener;
import smartin.miapi.item.ItemToModularConverter;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.conditions.*;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.properties.util.PropertyApplication;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.network.Networking;
import smartin.miapi.network.NetworkingImplCommon;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;

public class Miapi {
    public static final String MOD_ID = "miapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("ModularItem API");
    public static NetworkingImplCommon networkingImplementation;
    public static MinecraftServer server;
    public static Gson gson = new Gson();

    public static void init() {
        setupNetworking();
        setupDatapackPaths();
        RegistryInventory.setup();
        PropertyApplication.setup();
        ReloadEvents.setup();
        ItemAbilityManager.setup();
        AttributeRegistry.setup();
        ConditionManager.setup();
        LifecycleEvent.SERVER_BEFORE_START.register(minecraftServer -> {
            server = minecraftServer;
            LOGGER.info("Server before started");
        });
        ReloadListenerRegistry.register(ResourceType.SERVER_DATA, new ReloadListener());
        ReloadEvents.MAIN.subscribe((isClient) -> {
            RegistryInventory.modules.clear();
            ReloadEvents.DATA_PACKS.forEach(ItemModule::loadFromData);
        }, 0.0f);

        //MenuRegistry.registerScreenFactory(RegistryInventory.craftingScreenHandler, CraftingGUI::new);

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
            NbtCompound tag = itemStack.getNbt();
            try {
                String modulesString = tag.getString(ItemModule.moduleKey);
                Gson gson = new Gson();
                return gson.fromJson(modulesString, ItemModule.ModuleInstance.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        ModularItemCache.setSupplier(ItemModule.propertyKey, itemStack -> {
            return ItemModule.getUnmergedProperties((ItemModule.ModuleInstance) ModularItemCache.get(itemStack, ItemModule.moduleKey));
        });
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
        ConditionManager.moduleConditionRegistry.register("true", new TrueCondition());
        ConditionManager.moduleConditionRegistry.register("and", new AndCondition());
        ConditionManager.moduleConditionRegistry.register("or", new OrCondition());
        ConditionManager.moduleConditionRegistry.register("not", new NotCondition());
        ConditionManager.moduleConditionRegistry.register("material", new MaterialCondition());
        ConditionManager.moduleConditionRegistry.register("module", new ModuleTypeCondition());
        ConditionManager.moduleConditionRegistry.register("tag", new TagCondition());
        ConditionManager.moduleConditionRegistry.register("parent", new ParentCondition());
        ConditionManager.moduleConditionRegistry.register("child", new ChildCondition());
        ConditionManager.moduleConditionRegistry.register("otherModule", new OtherModuleModuleCondition());
    }

    protected static void setupNetworking() {
        networkingImplementation = new NetworkingImplCommon();
        Networking.setImplementation(networkingImplementation);
        networkingImplementation.setupServer();
    }

    protected static void setupDatapackPaths() {
        //DataPackPaths
        ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID, "modules");
        ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID, "materials");
        ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID, "synergies");
        ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID, "modular_converter");
        ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID, "skins");
    }
}
package smartin.miapi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartin.miapi.client.ClientInit;
import smartin.miapi.client.gui.crafting.CraftingGUI;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.datapack.ReloadListener;
import smartin.miapi.datapack.SpriteLoader;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.item.modular.items.*;
import smartin.miapi.item.modular.properties.*;
import smartin.miapi.item.modular.properties.render.GuiOffsetProperty;
import smartin.miapi.item.modular.properties.render.ModelMergeProperty;
import smartin.miapi.item.modular.properties.render.ModelProperty;
import smartin.miapi.item.modular.properties.render.ModelTransformationProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;

public class Miapi {
    public static final String MOD_ID = "miapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("ModularItem API");
    public static final MiapiRegistry<ModuleProperty> modulePropertyRegistry = MiapiRegistry.getInstance(ModuleProperty.class);
    public static final MiapiRegistry<ItemModule> moduleRegistry = MiapiRegistry.getInstance(ItemModule.class);
    public static final MiapiRegistry<Item> itemRegistry = MiapiRegistry.getInstance(Item.class);
    public static MinecraftServer server;
    public static Gson gson = new Gson();
    public static final ScreenHandlerType<CraftingScreenHandler> CRAFTING_SCREEN_HANDLER = register(new Identifier(Miapi.MOD_ID,"default_crafting"), CraftingScreenHandler::new);

    public static void init() {
        setupRegistries();
        ReloadEvents.setup();
        LifecycleEvent.SERVER_BEFORE_START.register(minecraftServer->{
            server = minecraftServer;
            LOGGER.info("Server before started");
        });
        LifecycleEvent.SERVER_STARTING.register(minecraftServer->{
            LOGGER.info("starting server");
        });
        SpriteLoader.setup();
        ReloadListenerRegistry.register(ResourceType.SERVER_DATA, new ReloadListener());
        ClientLifecycleEvent.CLIENT_STARTED.register(client->{
            new ClientInit();
        });
        ReloadEvents.MAIN.subscribe((isClient)->{
            moduleRegistry.clear();
            ReloadEvents.DATA_PACKS.forEach(ItemModule::loadFromData);
        },0.0f);
        Miapi.itemRegistry.addCallback(item ->   {
            Registry.register(Registry.ITEM, new Identifier(Miapi.itemRegistry.findKey(item)), item);
        });
        MenuRegistry.registerScreenFactory(CRAFTING_SCREEN_HANDLER,CraftingGUI::new);
        PropertyResolver.propertyProviderRegistry.register("module", (moduleInstance,oldMap) -> {
            HashMap<ModuleProperty, JsonElement> map = new HashMap<>();
            moduleInstance.module.getProperties().forEach((key, jsonData) -> {
                map.put(Miapi.modulePropertyRegistry.get(key),jsonData);
            });
            return map;
        });
        PropertyResolver.propertyProviderRegistry.register("moduleData", (moduleInstance, oldMap) -> {
            HashMap<ModuleProperty, JsonElement> map = new HashMap<>();
            String properties = moduleInstance.moduleData.get("properties");
            if(properties!=null){
                JsonObject moduleJson = gson.fromJson(properties, JsonObject.class);
                if(moduleJson!=null){
                    moduleJson.entrySet().forEach(stringJsonElementEntry -> {
                        ModuleProperty property = modulePropertyRegistry.get(stringJsonElementEntry.getKey());
                        if(property!=null){
                            map.put(property,stringJsonElementEntry.getValue());
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
    }

    protected static void setupRegistries(){
        //DataPackPaths
        ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID,"modules");
        ReloadEvents.registerDataPackPathToSync(Miapi.MOD_ID,"materials");

        //ITEM
        Miapi.itemRegistry.register(MOD_ID+":modular_item",new ExampleModularItem());
        Miapi.itemRegistry.register(MOD_ID+":modular_handle",new ModularWeapon());
        Miapi.itemRegistry.register(MOD_ID+":modular_sword",new ModularWeapon());
        Miapi.itemRegistry.register(MOD_ID+":modular_katana",new ModularWeapon());
        Miapi.itemRegistry.register(MOD_ID+":modular_greatsword",new ModularWeapon());
        Miapi.itemRegistry.register(MOD_ID+":modular_dagger",new ModularWeapon());
        Miapi.itemRegistry.register(MOD_ID+":modular_throwing_knife",new ModularWeapon());
        Miapi.itemRegistry.register(MOD_ID+":modular_rapier",new ModularWeapon());
        Miapi.itemRegistry.register(MOD_ID+":modular_longsword",new ModularWeapon());

        Miapi.itemRegistry.register(MOD_ID+":modular_bow",new ExampleModularBowItem());

        Miapi.itemRegistry.register(MOD_ID+":modular_helmet",new ModularHelmet());
        Miapi.itemRegistry.register(MOD_ID+":modular_chestplate",new ModularHelmet());
        Miapi.itemRegistry.register(MOD_ID+":modular_leggings",new ModularLeggings());
        Miapi.itemRegistry.register(MOD_ID+":modular_boots",new ModularBoots());


        Miapi.modulePropertyRegistry.register("moduleproperty1", (key,data) -> true);
        Miapi.modulePropertyRegistry.register("moduleproperty3", (key,data) -> true);
        //MODULEPROPERTIES
        Miapi.modulePropertyRegistry.register(NameProperty.KEY, new NameProperty());
        Miapi.modulePropertyRegistry.register(ModelProperty.KEY, new ModelProperty());
        Miapi.modulePropertyRegistry.register(SlotProperty.KEY, new SlotProperty());
        Miapi.modulePropertyRegistry.register(AllowedSlots.KEY,new AllowedSlots());
        Miapi.modulePropertyRegistry.register(MaterialProperty.KEY,new MaterialProperty());
        Miapi.modulePropertyRegistry.register(AllowedMaterial.KEY,new AllowedMaterial());
        Miapi.modulePropertyRegistry.register(AttributeProperty.KEY,new AttributeProperty());
        Miapi.modulePropertyRegistry.register(ModelTransformationProperty.KEY,new ModelTransformationProperty());
        Miapi.modulePropertyRegistry.register(DisplayNameProperty.KEY,new DisplayNameProperty());
        Miapi.modulePropertyRegistry.register(ItemIdProperty.KEY,new ItemIdProperty());
        Miapi.modulePropertyRegistry.register(GuiOffsetProperty.KEY,new GuiOffsetProperty());
        Miapi.modulePropertyRegistry.register(EquipmentSlotProperty.KEY,new EquipmentSlotProperty());
        Miapi.modulePropertyRegistry.register(ModelMergeProperty.KEY,new ModelMergeProperty());
    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(Identifier id, ScreenHandlerType.Factory<T> factory) {
        return (ScreenHandlerType) net.minecraft.util.registry.Registry.register(Registry.SCREEN_HANDLER, id, new ScreenHandlerType(factory));
    }
}
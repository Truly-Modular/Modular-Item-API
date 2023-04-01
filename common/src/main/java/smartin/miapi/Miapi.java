package smartin.miapi;

import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartin.miapi.client.ClientInit;
import smartin.miapi.client.gui.crafting.CraftingGUI;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.datapack.ReloadEvent;
import smartin.miapi.datapack.ReloadListener;
import smartin.miapi.datapack.SpriteLoader;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.item.modular.properties.NameProperty;
import smartin.miapi.item.modular.properties.SlotProperty;
import smartin.miapi.item.modular.properties.crafting.AllowedSlots;
import smartin.miapi.item.modular.properties.render.TextureProperty;
import smartin.miapi.registries.MiapiRegistry;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;

public class Miapi {
    public static final String MOD_ID = "miapi";
    public static final Logger LOGGER = LoggerFactory.getLogger("ModularItem API");
    public static final MiapiRegistry<ModuleProperty> modulePropertyRegistry = MiapiRegistry.getInstance(ModuleProperty.class);
    public static final MiapiRegistry<Item> itemRegistry = MiapiRegistry.getInstance(Item.class);
    public static final Item modularItem = new ModularItem();
    public static final Identifier modularItemIdentifier = new Identifier(MOD_ID, "modular_item");
    public static MinecraftServer server;

    public static final ScreenHandlerType<CraftingScreenHandler> CRAFTING_SCREEN_HANDLER = register(new Identifier(Miapi.MOD_ID,"default_crafting"), CraftingScreenHandler::new);


    public static void staticSetup(){

    }

    public static void init() {
        setupRegistries();
        ReloadEvent.setup();
        LifecycleEvent.SERVER_BEFORE_START.register(minecraftServer->{
            server = minecraftServer;
            LOGGER.info("Server before started");
        });
        LifecycleEvent.SERVER_STARTING.register(minecraftServer->{
            LOGGER.info("starting server");
        });
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register((listener)->{
            //SpriteLoader.LoadSprites(null);
        });
        SpriteLoader.setup();
        ReloadListenerRegistry.register(ResourceType.SERVER_DATA, new ReloadListener());
        ClientLifecycleEvent.CLIENT_STARTED.register(client->{
            new ClientInit();
        });
        MenuRegistry.registerScreenFactory(CRAFTING_SCREEN_HANDLER,CraftingGUI::new);
    }

    protected static void setupRegistries(){
        //ITEM
        Miapi.itemRegistry.register("modular_item",Miapi.modularItem);


        Miapi.modulePropertyRegistry.register("moduleproperty1", (key,data) -> true);
        Miapi.modulePropertyRegistry.register("moduleproperty3", (key,data) -> true);
        //MODULEPROPERTIES
        Miapi.modulePropertyRegistry.register(NameProperty.key, new NameProperty());
        Miapi.modulePropertyRegistry.register(TextureProperty.key, new TextureProperty());
        Miapi.modulePropertyRegistry.register(SlotProperty.key, new SlotProperty());
        Miapi.modulePropertyRegistry.register(AllowedSlots.key,new AllowedSlots());
    }

    public static NamedScreenHandlerFactory test(){
        //return Miapi.CRAFTING_SCREEN_HANDLER.create(213213,MinecraftClient.getInstance().player.getInventory());
        Miapi.LOGGER.warn("opening 2");
        Text text = Text.literal("test");
        return new SimpleNamedScreenHandlerFactory( (syncId, inventory, player) -> {
            Miapi.LOGGER.warn("opening 3");
            return new CraftingScreenHandler(syncId, inventory);
        }
                , text);
    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(Identifier id, ScreenHandlerType.Factory<T> factory) {
        return (ScreenHandlerType) net.minecraft.util.registry.Registry.register(Registry.SCREEN_HANDLER, id, new ScreenHandlerType(factory));
    }
}
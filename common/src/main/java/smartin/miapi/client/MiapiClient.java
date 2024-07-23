package smartin.miapi.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.redpxnda.nucleus.impl.ShaderRegistry;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientReloadShadersEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.item.Item;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchRenderer;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.client.model.ModularModelPredicateProvider;
import smartin.miapi.client.renderer.SpriteLoader;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.effects.CryoStatusEffect;
import smartin.miapi.entity.ItemProjectileRenderer;
import smartin.miapi.events.ClientEvents;
import smartin.miapi.modules.MiapiPermissions;
import smartin.miapi.modules.cache.CacheCommands;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialCommand;
import smartin.miapi.modules.material.MaterialIcons;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.material.palette.MaterialRenderControllers;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.network.Networking;
import smartin.miapi.registries.MiapiRegistry;
import smartin.miapi.registries.RegistryInventory;

import java.util.LinkedHashMap;
import java.util.Map;

import static smartin.miapi.registries.RegistryInventory.Client.glintShader;

public class MiapiClient {
    public static MaterialAtlasManager materialAtlasManager;
    public static boolean shaderModLoaded =
            Platform.isModLoaded("iris") ||
            Platform.isModLoaded("optifine") ||
            Platform.isModLoaded("optifabric") ||
            Platform.isModLoaded("oculus");
    public static boolean sodiumLoaded = isSodiumLoaded();
    public static boolean jerLoaded = Platform.isModLoaded("jeresources");
    public static final MiapiRegistry<KeyMapping> KEY_BINDINGS = MiapiRegistry.getInstance(KeyMapping.class);
    //public static final KeyBinding HOVER_DETAIL_BINDING = KEY_BINDINGS.register("miapi:hover_detail", new KeyBinding("miapi.gui.item_detail", 42, "miapi.keybinds"));

    private MiapiClient() {
    }

    public static void init() {
        RegistryInventory.modularItems.addCallback((MiapiClient::registerAnimations));
        //BoomerangClientRendering.setup();
        ClientTickEvent.CLIENT_PRE.register((instance -> {
            if (MiapiConfig.INSTANCE.client.other.animatedMaterials) {
                Minecraft.getInstance().getProfiler().push("miapiMaterialAnimations");
                MaterialSpriteManager.tick();
                Minecraft.getInstance().getProfiler().pop();
            }
        }));
        Networking.registerS2CPacket(MaterialCommand.SEND_MATERIAL_CLIENT, (buf -> {
            String materialId = buf.readUtf();
            Minecraft.getInstance().execute(() -> {
                Material material = MaterialProperty.materials.get(materialId);
                if (material != null) {
                    String raw = Miapi.gson.toJson(material.getDebugJson());
                    Component text = Component.literal(raw);
                    ClickEvent event = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, raw);
                    text = text.toFlatList(Style.EMPTY.withClickEvent(event)).get(0);
                    Minecraft.getInstance().player.sendSystemMessage(text);
                }
            });
        }));
        Networking.registerS2CPacket(CacheCommands.SEND_MATERIAL_CLIENT, (buf -> {
            Minecraft.getInstance().execute(ModularItemCache::discardCache);
        }));

        ClientReloadShadersEvent.EVENT.register((resourceFactory, shadersSink) -> {
            ModularItemCache.discardCache();
            if (Minecraft.getInstance().level != null) {
                Minecraft.getInstance().execute(() -> {
                    Map<String, String> cacheDatapack = new LinkedHashMap<>(ReloadEvents.DATA_PACKS);
                    ReloadEvents.reloadCounter++;
                    ReloadEvents.START.fireEvent(true);
                    ReloadEvents.DataPackLoader.trigger(cacheDatapack);
                    ReloadEvents.MAIN.fireEvent(true);
                    ReloadEvents.END.fireEvent(true);
                    ReloadEvents.reloadCounter--;
                    ModularItemCache.discardCache();
                });
            }
        });

        registerShaders();
        MaterialRenderControllers.setup();
        MaterialIcons.setup();
        ColorProvider.setup();
        ClientLifecycleEvent.CLIENT_SETUP.register(MiapiClient::clientSetup);
        ClientLifecycleEvent.CLIENT_STARTED.register(MiapiClient::clientStart);
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(MiapiClient::clientLevelLoad);
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> new Thread(() -> MiapiPermissions.getPerms(player)).start());
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> {
            ModularItemCache.discardCache();
            if (jerLoaded && Miapi.server == null) {
                String version = Platform.getMod("jeresources").getVersion();
                if (version.equals("1.4.0.238") || version.equals("1.4.0.246") || version.equals("1.4.0.247")) {
                    player.sendSystemMessage(Component.literal("Just Enough Resources 1.20.1-1.4.0.247 Release is broken on servers. Please Remove it."));
                    ClickEvent event = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/way2muchnoise/JustEnoughResources/issues/392");
                    Component link = Component.literal("For more information you can read this");
                    player.sendSystemMessage(link.toFlatList(Style.EMPTY.withClickEvent(event).withUnderlined(true)).get(0));
                    player.sendSystemMessage(Component.literal("This message was sent by Truly Modular."));
                }
            }
        });
        ClientReloadShadersEvent.EVENT.register((resourceFactory, asd) -> ModularItemCache.discardCache());
        RegistryInventory.modularItems.addCallback((item -> {
            ModularModelPredicateProvider.registerModelOverride(item, Miapi.id("damage"), (stack, world, entity, seed) -> stack.isDamageableItem() && stack.getDamageValue() > 0 ? ((float) stack.getDamageValue() / stack.getMaxDamage()) : 0.0f);
            ModularModelPredicateProvider.registerModelOverride(item, Miapi.id( "damaged"), (stack, world, entity, seed) -> stack.isDamaged() ? 1.0F : 0.0F);
        }));
        ReloadEvents.START.subscribe(isClient -> {
            if (isClient) {
                StatListWidget.onReload();
            }
        });
        ReloadEvents.END.subscribe(isClient -> {
            if (isClient) {
                StatListWidget.reloadEnd();
            }
        });
        if (sodiumLoaded) {
            ClientEvents.HUD_RENDER.register((drawContext, deltaTick) -> MaterialSpriteManager.onHudRender(drawContext));
        }
    }

    @Environment(EnvType.CLIENT)
    public static void registerAnimations(Item item) {
        ModularModelPredicateProvider.registerModelOverride(item, Miapi.id( "damage"), (stack, world, entity, seed) -> {
            return stack.isDamageableItem() && stack.isDamaged() ? 0.0f : (float) stack.getMaxDamage() / (stack.getMaxDamage() - stack.getDamageValue());
        });
        ModularModelPredicateProvider.registerModelOverride(item, Miapi.id( "damaged"), (stack, world, entity, seed) -> {
            return stack.isDamaged() ? 0.0f : 1.0f;
        });
    }

    public static boolean isSodiumLoaded() {
        return Platform.isModLoaded("sodium") ||
               Platform.isModLoaded("embeddium") ||
               Platform.isModLoaded("magnesium") ||
               Platform.isModLoaded("rubidium");
    }

    public static boolean isHigherVersion(String version, String compareToVersion) {
        String[] versionParts = version.split("\\.");
        String[] compareToVersionParts = compareToVersion.split("\\.");

        for (int i = 0; i < versionParts.length && i < compareToVersionParts.length; i++) {
            int part1 = Integer.parseInt(versionParts[i]);
            int part2 = Integer.parseInt(compareToVersionParts[i]);

            if (part1 > part2) {
                return true;
            } else if (part1 < part2) {
                return false;
            }
        }

        // If all common parts are equal, the longer version is considered higher
        return versionParts.length > compareToVersionParts.length;
    }

    protected static void clientSetup(Minecraft client) {
        runOnClientEnsured(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.getTextureManager();
            SpriteLoader.setup();
        });
    }

    public static void runOnClientEnsured(Runnable runnable) {
        if (RenderSystem.isOnRenderThreadOrInit()) {
            runnable.run();
        } else {
            Minecraft mc = Minecraft.getInstance();
            mc.execute(runnable);
        }
    }

    protected static void clientStart(Minecraft client) {
        runOnClientEnsured(() -> {
            Minecraft mc = Minecraft.getInstance();
            //Load StatDisplayClass
            mc.getTextureManager();
            materialAtlasManager = new MaterialAtlasManager(mc.getTextureManager());
            ((ReloadableResourceManager) mc.getResourceManager()).registerReloadListener(materialAtlasManager);
            //((ReloadableResourceManagerImpl) mc.getResourceManager()).registerReloader(new AltModelAtlasManager(mc.getTextureManager()));
            CryoStatusEffect.setupOnClient();
        });
    }

    protected static void clientLevelLoad(ClientLevel clientWorld) {
        SpriteLoader.clientStart();
        ModularItemCache.discardCache();
    }

    public static void registerScreenHandler() {
        MenuRegistry.registerScreenFactory(RegistryInventory.craftingScreenHandler, CraftingScreen::new);
    }

    public static void registerEntityRenderer() {
        EntityRendererRegistry.register(RegistryInventory.itemProjectileType, ItemProjectileRenderer::new);
    }

    public static void registerBlockEntityRenderer() {
        BlockEntityRendererRegistry.register(RegistryInventory.modularWorkBenchEntityType, ModularWorkBenchRenderer::new);
    }

    public static void registerShaders() {
        /*ShaderRegistry.register(
                new Identifier(Miapi.MOD_ID, "rendertype_translucent_material"),
                VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, s -> RegistryInventory.Client.translucentMaterialShader = s);*/

        runOnClientEnsured(() -> {
            ShaderRegistry.register(
                    Miapi.id( "rendertype_entity_translucent_material"),
                    DefaultVertexFormat.NEW_ENTITY, s -> RegistryInventory.Client.entityTranslucentMaterialShader = s);
            ShaderRegistry.register(
                    Miapi.id("rendertype_item_glint"),
                    DefaultVertexFormat.NEW_ENTITY, s -> glintShader = s);
        });
    }
}

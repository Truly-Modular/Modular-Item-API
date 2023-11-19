package smartin.miapi.client;

import com.redpxnda.nucleus.impl.ShaderRegistry;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientReloadShadersEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchRenderer;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.client.model.CustomColorProvider;
import smartin.miapi.client.model.ModularModelPredicateProvider;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.effects.CryoStatusEffect;
import smartin.miapi.entity.ItemProjectileRenderer;
import smartin.miapi.mixin.client.ItemRendererAccessor;
import smartin.miapi.modules.MiapiPermissions;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.material.MaterialIcons;
import smartin.miapi.modules.material.palette.PaletteCreators;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.registries.RegistryInventory;

import static smartin.miapi.registries.RegistryInventory.Client.glintShader;

public class MiapiClient {
    public static MaterialAtlasManager materialAtlasManager;
    public static boolean irisLoaded = Platform.isModLoaded("iris");
    public static boolean sodiumLoaded = Platform.isModLoaded("sodium");

    private MiapiClient() {
    }

    public static void init() {
        registerShaders();
        PaletteCreators.setup();
        MaterialIcons.setup();
        ColorProvider.setup();
        ClientLifecycleEvent.CLIENT_SETUP.register(MiapiClient::clientSetup);
        ClientLifecycleEvent.CLIENT_STARTED.register(MiapiClient::clientStart);
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(MiapiClient::clientLevelLoad);
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> new Thread(() -> MiapiPermissions.getPerms(player)).start());
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> {
            if (irisLoaded && MiapiConfig.CompatGroup.sendWarningOnWorldLoad.getValue()) {
                player.sendMessage(Text.literal("Truly Modulars rendering is switched to Fallback."));
                player.sendMessage(Text.literal("This means Modular Items will look significantly worse than they are supposed to."));
                player.sendMessage(Text.literal("This is due to Iris not allowing Mods to implement custom shaders."));
                ClickEvent event = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/IrisShaders/Iris/blob/1.20.1/docs/development/compatibility/core-shaders.md");
                Text link = Text.literal("For more information you can read this");
                player.sendMessage(Text.literal("You can disable this warning and switch back to the default renderer in the Config."));
                link = link.getWithStyle(Style.EMPTY.withClickEvent(event).withUnderline(true)).get(0);
                player.sendMessage(link);
            }
        });
        ClientReloadShadersEvent.EVENT.register((resourceFactory, asd) -> ModularItemCache.discardCache());
        RegistryInventory.modularItems.addCallback((item -> {
            ModularModelPredicateProvider.registerModelOverride(item, new Identifier(Miapi.MOD_ID, "damage"), (stack, world, entity, seed) -> {
                return stack.isDamageable() && stack.getDamage() > 0 ? ((float) stack.getDamage() / stack.getMaxDamage()) : 0.0f;
            });
            ModularModelPredicateProvider.registerModelOverride(item, new Identifier(Miapi.MOD_ID, "damaged"), (stack, world, entity, seed) -> stack.isDamaged() ? 1.0F : 0.0F);
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
    }

    protected static void clientSetup(MinecraftClient client) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getTextureManager();
        SpriteLoader.setup();
    }

    protected static void clientStart(MinecraftClient client) {
        MinecraftClient mc = MinecraftClient.getInstance();
        //Load StatDisplayClass
        mc.getTextureManager();
        materialAtlasManager = new MaterialAtlasManager(mc.getTextureManager());
        ((ReloadableResourceManagerImpl) mc.getResourceManager()).registerReloader(materialAtlasManager);
        RegistryInventory.addCallback(RegistryInventory.modularItems, item -> {
            ((ItemRendererAccessor) client.getItemRenderer()).color().register(new CustomColorProvider(), item);
        });
        CryoStatusEffect.setupOnClient();
    }

    protected static void clientLevelLoad(ClientWorld clientWorld) {
        SpriteLoader.clientStart();
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
        ShaderRegistry.register(
                new Identifier(Miapi.MOD_ID, "rendertype_entity_translucent_material"),
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, s -> RegistryInventory.Client.entityTranslucentMaterialShader = s);
        ShaderRegistry.register(
                new Identifier(Miapi.MOD_ID, "rendertype_item_glint"),
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, s -> glintShader = s);
    }
}

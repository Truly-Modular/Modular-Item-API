package smartin.miapi.client;

import com.redpxnda.nucleus.impl.ShaderRegistry;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientReloadShadersEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchRenderer;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.model.CustomColorProvider;
import smartin.miapi.client.model.ModularModelPredicateProvider;
import smartin.miapi.effects.CryoStatusEffect;
import smartin.miapi.entity.ItemProjectileRenderer;
import smartin.miapi.mixin.client.ItemRendererAccessor;
import smartin.miapi.modules.MiapiPermissions;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.material.MaterialIcons;
import smartin.miapi.modules.properties.material.PaletteCreators;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.registries.RegistryInventory;

import static smartin.miapi.registries.RegistryInventory.Client.glintShader;

public class MiapiClient {
    public static MaterialAtlasManager materialAtlasManager;

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
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(MiapiPermissions::getPerms);
        ClientReloadShadersEvent.EVENT.register((resourceFactory, asd) -> ModularItemCache.discardCache());
        RegistryInventory.modularItems.addCallback((item -> {
            ModularModelPredicateProvider.registerModelOverride(item, new Identifier(Miapi.MOD_ID, "damage"), (stack, world, entity, seed) -> {
                return stack.isDamageable() && stack.getDamage() > 0 ? ((float) stack.getDamage() / stack.getMaxDamage()) : 0.0f;
            });
            ModularModelPredicateProvider.registerModelOverride(item, new Identifier(Miapi.MOD_ID, "damaged"), (stack, world, entity, seed) -> stack.isDamaged() ? 1.0F : 0.0F);
        }));
    }

    protected static void clientSetup(MinecraftClient client) {
        Miapi.DEBUG_LOGGER.error("CLIENT SETUP");
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getTextureManager();
        //materialAtlasManager = new MaterialAtlasManager(mc.getTextureManager());
        //((ReloadableResourceManagerImpl) mc.getResourceManager()).registerReloader(materialAtlasManager);
        SpriteLoader.setup();
    }

    protected static void clientStart(MinecraftClient client) {
        Miapi.DEBUG_LOGGER.error("CLIENT START");
        MinecraftClient mc = MinecraftClient.getInstance();
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

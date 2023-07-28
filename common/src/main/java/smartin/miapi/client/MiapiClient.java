package smartin.miapi.client;

import com.redpxnda.nucleus.impl.ShaderRegistry;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchRenderer;
import smartin.miapi.client.gui.crafting.CraftingGUI;
import smartin.miapi.client.model.CustomColorProvider;
import smartin.miapi.effects.CryoStatusEffect;
import smartin.miapi.mixin.client.ItemRendererAccessor;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectileRenderer;
import smartin.miapi.registries.RegistryInventory;

public class MiapiClient {

    private MiapiClient() {
    }

    public static void init(){
        registerShaders();
        ClientLifecycleEvent.CLIENT_SETUP.register(MiapiClient::clientSetup);
        ClientLifecycleEvent.CLIENT_STARTED.register(MiapiClient::clientStart);
        ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(MiapiClient::clientLevelLoad);
    }
    protected static void clientSetup(MinecraftClient client){
        SpriteLoader.setup();
    }

    protected static void clientStart(MinecraftClient client) {
        RegistryInventory.addCallback(RegistryInventory.modularItems, item -> {
            ((ItemRendererAccessor) client.getItemRenderer()).color().register(new CustomColorProvider(), item);
        });
        CryoStatusEffect.setupOnClient();
    }

    protected static void clientLevelLoad(ClientWorld clientWorld){
        SpriteLoader.clientStart();
    }

    public static void registerScreenHandler() {
        MenuRegistry.registerScreenFactory(RegistryInventory.craftingScreenHandler, CraftingGUI::new);
    }

    public static void registerEntityRenderer() {
        EntityRendererRegistry.register(RegistryInventory.itemProjectileType, ItemProjectileRenderer::new);
    }

    public static void registerBlockEntityRenderer() {
        BlockEntityRendererRegistry.register(RegistryInventory.modularWorkBenchEntityType, ModularWorkBenchRenderer::new);
    }

    public static void registerShaders() {
        ShaderRegistry.register(
                new Identifier(Miapi.MOD_ID, "rendertype_translucent_material"),
                VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, s -> RegistryInventory.Client.translucentMaterialShader = s);
        ShaderRegistry.register(
                new Identifier(Miapi.MOD_ID, "rendertype_entity_translucent_material"),
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, s -> RegistryInventory.Client.entityTranslucentMaterialShader = s);
    }
}

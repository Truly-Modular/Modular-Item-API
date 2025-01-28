package smartin.miapi.mixin.client;

import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.atlas.MaterialAtlasManager;

@Mixin(GuiSpriteManager.class)
public class GuiSpritesManagerMixin {

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void miapi$customItemRenderingEntityGetter(TextureManager textureManager, CallbackInfo ci) {
        //Minecraft client = (Minecraft) (Object) this;
        MiapiClient.materialAtlasManager = new MaterialAtlasManager(textureManager);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, MiapiClient.materialAtlasManager);
        //((ReloadableResourceManager) client.getResourceManager()).registerReloadListener(MiapiClient.materialAtlasManager);
    }
}

package smartin.miapi.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.atlas.VertexConsumerProvider;
import smartin.miapi.modules.ModuleInstance;

import java.io.Closeable;

/**
 * This takes control of the rendering with a Material
 * You probably want to extend the {@link SpritePixelReplacer} instead
 */
@Environment(EnvType.CLIENT)
public interface MaterialRenderController extends Closeable {

    /**
     * This allows Materials to pick their own Vertexconsumers, allowing for all kinds of fancy Animations.
     * We sadly cant parse the model into it, since there is no guarantee about what the model is
     */
    void getVertexConsumer(
            TextureAtlasSprite originalSprite,
            ItemStack stack,
            ModuleInstance moduleInstance,
            ItemDisplayContext mode, VertexConsumerProvider out);

    /**
     * getVertexConsumer a simple Color of the Material for other purposes, namely the material description gui
     */
    Color getAverageColor();
}

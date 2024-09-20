package smartin.miapi.material.palette;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.util.Color;
import smartin.miapi.modules.ModuleInstance;

import java.io.Closeable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * This takes control of the rendering with a Material
 * You probably want to extend the {@link SpritePixelReplacer} instead
 */
public interface MaterialRenderController extends Closeable {

    /**
     * This allows Materials to pick their own Vertexconsumers, allowing for all kinds of fancy Animations.
     * We sadly cant parse the model into it, since there is no guarantee about what the model is
     */
    VertexConsumer getVertexConsumer(
            MultiBufferSource vertexConsumers,
            TextureAtlasSprite originalSprite,
            ItemStack stack,
            ModuleInstance moduleInstance,
            ItemDisplayContext mode);

    /**
     * getVertexConsumer a simple Color of the Material for other purposes, namely the material description gui
     */
    Color getAverageColor();
}

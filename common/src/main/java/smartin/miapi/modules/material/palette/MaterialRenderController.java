package smartin.miapi.modules.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;

import java.io.Closeable;

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
            VertexConsumerProvider vertexConsumers,
            Sprite originalSprite,
            ItemStack stack,
            ModuleInstance moduleInstance,
            ModelTransformationMode mode);

    /**
     * getVertexConsumer a simple Color of the Material for other purposes, namely the material description gui
     */
    Color getAverageColor();
}

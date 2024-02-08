package smartin.miapi.modules.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;

/**
 * This takes control of the rendering with a Material
 * You probably want to extend the {@link SimpleMaterialPalette} instead
 */
public interface MaterialColorer {

    /**
     * This allows Materials to pick their own Vertexconsumers, allowing for all kinds of fancy Animations.
     * We sadly cant parse the model into it, since in the Future we might want to use different Models that are not BakedModels as well
     * @param vertexConsumers
     * @param stack
     * @param moduleInstance
     * @param mode
     * @return
     */
    VertexConsumer getVertexConsumer(
            VertexConsumerProvider vertexConsumers,
            Sprite originalSprite,
            ItemStack stack,
            ModuleInstance moduleInstance,
            ModelTransformationMode mode);

    /**
     * get a simple Color of the Material for other purposes
     * @return
     */
    Color getAverageColor();
}

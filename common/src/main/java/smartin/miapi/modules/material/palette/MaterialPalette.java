package smartin.miapi.modules.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.modules.ModuleInstance;

public interface MaterialPalette {
    @Environment(EnvType.CLIENT)
    @Nullable
    SpriteContents generateSpriteContents(Identifier id);

    @Environment(EnvType.CLIENT)
    @Nullable
    Identifier getSpriteId();

    @Environment(EnvType.CLIENT)
    void setSpriteId(Identifier id);

    @Environment(EnvType.CLIENT)
    Color getPaletteAverageColor();

    /**
     * if the SpriteContents should be uploaded and Managed by the {@link MaterialAtlasManager}
     * @return true if the Material Atlas should be used
     */
    default boolean useMaterialAtlas(){
        return true;
    }

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
}

package smartin.miapi.modules.properties.material.palette;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;

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

    @Environment(EnvType.CLIENT)
    VertexConsumer getVertexConsumer(VertexConsumerProvider vertexConsumers, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode);
}

package smartin.miapi.modules.properties.render.colorproviders;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;

public class PotionColorProvider implements ColorProvider {
    Color potioncolor;

    public PotionColorProvider() {

    }

    public PotionColorProvider(ItemStack stack) {
        if (!stack.has(DataComponents.POTION_CONTENTS)) {
            potioncolor = Color.WHITE;
        } else {
            potioncolor = new Color(stack.getComponents().get(DataComponents.POTION_CONTENTS).getColor());
        }
    }

    @Override
    public Optional<Color> getVertexColor() {
        return Optional.of(potioncolor);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public VertexConsumer getConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, ItemStack stack, ModuleInstance moduleInstance, ItemDisplayContext mode) {
        return vertexConsumers.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true));
    }

    @Override
    public ColorProvider getInstance(ItemStack stack, ModuleInstance instance, TrimRenderer.TrimMode trimMode) {
        return new smartin.miapi.modules.properties.render.colorproviders.PotionColorProvider(stack);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        smartin.miapi.modules.properties.render.colorproviders.PotionColorProvider that = (smartin.miapi.modules.properties.render.colorproviders.PotionColorProvider) obj;
        return java.util.Objects.equals(potioncolor, that.potioncolor);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(potioncolor);
    }

}

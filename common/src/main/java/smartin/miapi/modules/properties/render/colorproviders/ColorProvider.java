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
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * This class deals with recoloring models
 */
public interface ColorProvider {
    Map<String, ColorProvider> colorProviders = new HashMap<>();


    static void setup() {
        colorProviders.put("material", new MaterialColorProvider());
        colorProviders.put("model", new ModelColorProvider());
        colorProviders.put("potion", new PotionColorProvider());
        colorProviders.put("parent", new ParentColorProvider());
    }

    static ColorProvider getProvider(String type, ItemStack itemStack, ModuleInstance moduleInstance) {
        return colorProviders.getOrDefault(type, colorProviders.get("material")).getInstance(itemStack, moduleInstance);
    }

    default Color getVertexColor() {
        return Color.WHITE;
    }

    @Environment(EnvType.CLIENT)
    VertexConsumer getConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, ItemStack stack, ModuleInstance moduleInstance, ItemDisplayContext mode);

    ColorProvider getInstance(ItemStack stack, ModuleInstance instance);

    class MaterialColorProvider implements ColorProvider {
        public Material material;

        public MaterialColorProvider() {
        }

        public MaterialColorProvider(Material material) {
            this.material = material;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public VertexConsumer getConsumer(MultiBufferSource vertexConsumers,
                                          TextureAtlasSprite sprite,
                                          ItemStack stack,
                                          ModuleInstance moduleInstance,
                                          ItemDisplayContext mode) {
            return material.getRenderController().getVertexConsumer(vertexConsumers,sprite, stack, moduleInstance, mode);
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ModuleInstance instance) {
            Material material1 = MaterialProperty.getMaterial(instance);
            if (material1 != null) {
                return new MaterialColorProvider(material1);
            }
            return new ModelColorProvider();
        }
    }

    class ParentColorProvider extends MaterialColorProvider {
        @Override
        public ColorProvider getInstance(ItemStack stack, ModuleInstance instance) {
            if (instance.parent != null) {
                return super.getInstance(stack, instance.parent);
            }
            return new ModelColorProvider(stack);
        }
    }

    class ModelColorProvider implements ColorProvider {
//        ItemStack stack = ItemStack.EMPTY;

        public ModelColorProvider() {
        }

        public ModelColorProvider(ItemStack stack) {
//            this.stack = stack;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public VertexConsumer getConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, ItemStack stack, ModuleInstance moduleInstance, ItemDisplayContext mode) {
            return vertexConsumers.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true));
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ModuleInstance instance) {
            return new ModelColorProvider(stack);
        }
    }

    class PotionColorProvider implements ColorProvider {
        Color potioncolor;

        public PotionColorProvider() {

        }

        public PotionColorProvider(ItemStack stack) {
            potioncolor = new Color( stack.getComponents().get(DataComponents.POTION_CONTENTS).getColor());
        }

        @Override
        public Color getVertexColor() {
            return potioncolor;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public VertexConsumer getConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, ItemStack stack, ModuleInstance moduleInstance, ItemDisplayContext mode) {
            return vertexConsumers.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true));
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ModuleInstance instance) {
            return new PotionColorProvider(stack);
        }
    }
}

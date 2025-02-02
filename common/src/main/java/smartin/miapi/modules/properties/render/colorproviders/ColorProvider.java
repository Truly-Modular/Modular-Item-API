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
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        ColorProvider base = colorProviders.getOrDefault(type, colorProviders.get("material"));
        return base.getInstance(itemStack, base.adapt(moduleInstance));
    }

    default Optional<Color> getVertexColor() {
        return Optional.empty();
    }

    default ModuleInstance adapt(ModuleInstance moduleInstance) {
        return moduleInstance;
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
            return material.getRenderController(moduleInstance, mode).getVertexConsumer(vertexConsumers, sprite, stack, moduleInstance, mode);
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
        public Material material;

        public ParentColorProvider() {
        }

        public ParentColorProvider(Material material) {
            this.material = material;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public VertexConsumer getConsumer(MultiBufferSource vertexConsumers,
                                          TextureAtlasSprite sprite,
                                          ItemStack stack,
                                          ModuleInstance moduleInstance,
                                          ItemDisplayContext mode) {
            return material.getRenderController(moduleInstance, mode).getVertexConsumer(vertexConsumers, sprite, stack, moduleInstance, mode);
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ModuleInstance instance) {
            Material material1 = MaterialProperty.getMaterial(instance);
            if (material1 != null) {
                return new ParentColorProvider(material1);
            }
            return new ModelColorProvider();
        }

        public ModuleInstance adapt(ModuleInstance moduleInstance) {
            if (moduleInstance.parent != null) {
                return moduleInstance.parent;
            }
            return moduleInstance;
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
        public ColorProvider getInstance(ItemStack stack, ModuleInstance instance) {
            return new PotionColorProvider(stack);
        }
    }
}

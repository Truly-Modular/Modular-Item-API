package smartin.miapi.modules.properties.render.ColorProviders;

import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

public interface ColorProvider {
    Map<String, ColorProvider> colorProviders = new HashMap<>();


    static void setup() {
        colorProviders.put("material", new MaterialColorProvider());
        colorProviders.put("model", new ModelColorProvider());
    }

    static ColorProvider getProvider(String type, ItemStack itemStack, ItemModule.ModuleInstance moduleInstance) {
        return colorProviders.getOrDefault(type, colorProviders.get("material")).getInstance(itemStack, moduleInstance);
    }

    VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers);

    ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance);

    class MaterialColorProvider implements ColorProvider {
        Material material;

        public MaterialColorProvider() {
        }

        public MaterialColorProvider(Material material) {
            this.material = material;
        }

        @Override
        public VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers) {
            return material.setupMaterialShader(vertexConsumers, RegistryInventory.Client.entityTranslucentMaterialRenderType, RegistryInventory.Client.entityTranslucentMaterialShader);
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance) {
            Material material1 = MaterialProperty.getMaterial(instance);
            if (material1 != null) {
                return new MaterialColorProvider(material1);
            }
            return new ModelColorProvider();
        }
    }

    class ModelColorProvider implements ColorProvider {
        ItemStack stack = ItemStack.EMPTY;

        public ModelColorProvider() {
        }

        public ModelColorProvider(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers) {
            return vertexConsumers.getBuffer(RenderLayers.getItemLayer(stack, true));
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance) {
            return new ModelColorProvider(stack);
        }
    }
}

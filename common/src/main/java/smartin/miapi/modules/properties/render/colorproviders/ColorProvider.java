package smartin.miapi.modules.properties.render.colorproviders;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import smartin.miapi.client.MaterialVertexConsumer;
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
        colorProviders.put("potion", new PotionColorProvider());
    }

    static ColorProvider getProvider(String type, ItemStack itemStack, ItemModule.ModuleInstance moduleInstance) {
        return colorProviders.getOrDefault(type, colorProviders.get("material")).getInstance(itemStack, moduleInstance);
    }

    default Color getVertexColor() {
        return Color.WHITE;
    }

    @Environment(EnvType.CLIENT)
    VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers);

    ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance);

    class MaterialColorProvider implements ColorProvider {
        Material material;

        public MaterialColorProvider() {
        }

        public MaterialColorProvider(Material material) {
            this.material = material;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers) {
            return new MaterialVertexConsumer(vertexConsumers.getBuffer(RegistryInventory.Client.entityTranslucentMaterialRenderType), material);
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

        @Environment(EnvType.CLIENT)
        @Override
        public VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers) {
            return vertexConsumers.getBuffer(RenderLayers.getItemLayer(stack, true));
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance) {
            return new ModelColorProvider(stack);
        }
    }

    class PotionColorProvider implements ColorProvider {
        Color potioncolor;
        ItemStack stack;

        public PotionColorProvider() {

        }

        public PotionColorProvider(ItemStack stack) {
            potioncolor = new Color(PotionUtil.getColor(stack));
            this.stack = stack;
        }

        @Override
        public Color getVertexColor() {
            return potioncolor;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers) {
            return vertexConsumers.getBuffer(RenderLayers.getItemLayer(stack, true));
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance) {
            return new PotionColorProvider(stack);
        }
    }
}

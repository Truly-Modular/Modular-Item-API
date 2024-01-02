package smartin.miapi.modules.properties.render.colorproviders;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.MaterialAtlasManager;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.mixin.client.SpriteContentsAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.material.palette.MaterialPalette;

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
    VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode);

    ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance);

    SpriteContents tranform(SpriteContents contents);

    class MaterialColorProvider implements ColorProvider {
        Material material;

        public MaterialColorProvider() {
        }

        public MaterialColorProvider(Material material) {
            this.material = material;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode) {
            return material.getVertexConsumer(vertexConsumers, stack, moduleInstance, mode);
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance) {
            Material material1 = MaterialProperty.getMaterial(instance);
            if (material1 != null) {
                return new MaterialColorProvider(material1);
            }
            return new ModelColorProvider();
        }

        @Override
        public SpriteContents tranform(SpriteContents contents) {
            MaterialPalette palette = material.getPalette();
            NativeImage rawImage = ((SpriteContentsAccessor) contents).getImage();
            NativeImage image = new NativeImage(contents.getWidth(), contents.getHeight(), false);
            for (int x = 0; x < rawImage.getWidth(); x++) {
                for (int y = 0; y < rawImage.getHeight(); y++) {
                    rawImage.getColor(x, y);
                    image.setColor(x, y, getColor(material, rawImage.getRed(x, y)));
                    //image.setColor(x, y, 1);
                }
            }
            Identifier newID = new Identifier(Miapi.MOD_ID, contents.getId().getPath() + "_transformed_" + material.getKey());
            return new SpriteContents(
                    newID,
                    new SpriteDimensions(
                            ((SpriteContentsAccessor) contents).getHeight(),
                            ((SpriteContentsAccessor) contents).getHeight()),
                    image,
                    AnimationResourceMetadata.EMPTY);
        }

        private int getColor(Material material, int color) {
            Sprite sprite = MiapiClient.materialAtlasManager.getMaterialSprite(material.getPalette().getSpriteId());
            if (sprite == null) {
                sprite = MiapiClient.materialAtlasManager.getMaterialSprite(MaterialAtlasManager.BASE_MATERIAL_ID);
            }
            return ((SpriteContentsAccessor) sprite.getContents()).getImage().getColor(Math.max(color, 255), 0);
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
        public VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode) {
            return vertexConsumers.getBuffer(RenderLayers.getItemLayer(stack, true));
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance) {
            return new ModelColorProvider(stack);
        }

        @Override
        public SpriteContents tranform(SpriteContents contents) {
            return contents;
        }
    }

    class PotionColorProvider implements ColorProvider {
        Color potioncolor;

        public PotionColorProvider() {

        }

        public PotionColorProvider(ItemStack stack) {
            potioncolor = new Color(PotionUtil.getColor(stack));
        }

        @Override
        public Color getVertexColor() {
            return potioncolor;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode) {
            return vertexConsumers.getBuffer(RenderLayers.getItemLayer(stack, true));
        }

        @Override
        public ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance) {
            return new PotionColorProvider(stack);
        }

        @Override
        public SpriteContents tranform(SpriteContents contents) {
            return contents;
        }
    }
}

package smartin.miapi.modules.properties.render.overlay;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.BakedMiapiModel;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.material.properties.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.baked.ModelProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.modules.properties.render.colorproviders.MaterialColorProvider;

import java.util.List;
import java.util.Optional;


public class OverlayModelProperty extends AttachedModelProperty<OverlayModelProperty.OverlayModelData> {

    public static final ResourceLocation KEY = Miapi.id("overlay_texture_model");
    public static OverlayModelProperty property;

    public OverlayModelProperty() {
        super(OverlayModelData.CODEC);
        property = this;
    }

    /**
     * Overlay-specific attachment data.
     */
    public static class OverlayModelData extends CustomData {
        public static final MapCodec<OverlayModelData> CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.STRING.optionalFieldOf("texture").forGetter(d -> Optional.ofNullable(d.texture)),
                        Codec.STRING.fieldOf("colorProvider").forGetter(d -> d.colorProvider)
                ).apply(instance, OverlayModelData::new));

        @Nullable
        public final String texture;
        public final String colorProvider;

        @Nullable
        private TextureAtlasSprite cachedSprite;

        public OverlayModelData(Optional<String> texture, String colorProvider) {
            this.texture = texture.orElse(null);
            this.colorProvider = colorProvider;
        }

        @Override
        public void preload() {
            if (texture != null) {
                cachedSprite = ModelProperty.textureGetter.apply(
                        new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(texture))
                );
            }
        }

        public boolean useThisModule() {
            return !colorProvider.equals("other");
        }

        @Override
        @Nullable
        public List<MiapiModel> createModel(ItemStack stack, ModuleInstance base, ModuleInstance source, ModelHolder holder) {
            TextureAtlasSprite overWriteSprite = cachedSprite != null ? cachedSprite :
                    (texture != null
                            ? ModelProperty.textureGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(texture)))
                            : null);

            ColorProvider colorProviderInstance = getColorProvider(stack, base, source, holder.colorProvider(), holder.trimMode());

            return List.of(new BakedMiapiModel(
                    new ModelHolder(
                            holder.model(),
                            new Matrix4f(holder.matrix4f()),
                            new ColorProvider() {
                                @Override
                                public VertexConsumer getConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, ItemStack stack, ModuleInstance moduleInstance, ItemDisplayContext mode) {

                                    return colorProviderInstance.getConsumer(vertexConsumers,
                                                    overWriteSprite == null ? sprite : overWriteSprite,
                                                    stack,
                                                    useThisModule() ? base :
                                                            moduleInstance, mode);
                                }

                                @Override
                                public ColorProvider getInstance(ItemStack stack, ModuleInstance instance, TrimRenderer.TrimMode trimMode) {
                                    return this;
                                }
                            },
                            new int[]{-1, -1},
                            holder.trimMode(),
                            holder.entityRendering()
                    ), useThisModule() ? base : source, stack));
        }

        private ColorProvider getColorProvider(ItemStack stack, ModuleInstance current, ModuleInstance other, ColorProvider otherColor, TrimRenderer.TrimMode mode) {
            switch (colorProvider) {
                case "this" -> {
                    return ColorProvider.getProvider("material", stack, current, mode);
                }
                case "other" -> {
                    return otherColor;
                }
                default -> {
                    if (ColorProvider.colorProviders.containsKey(colorProvider)) {
                        return ColorProvider.getProvider(colorProvider, stack, current, mode);
                    }
                    if (colorProvider.startsWith("material:")) {
                        ResourceLocation materialId = ResourceLocation.parse(colorProvider.split(":", 2)[1]);
                        smartin.miapi.material.base.Material material = MaterialProperty.MATERIAL_REGISTRY.get(materialId);
                        if (material != null) {
                            return new MaterialColorProvider(material, mode);
                        }
                        Miapi.LOGGER.error("Could not find Material " + materialId + " for Color Provider ");
                    }
                }
            }
            return otherColor;
        }
    }
}

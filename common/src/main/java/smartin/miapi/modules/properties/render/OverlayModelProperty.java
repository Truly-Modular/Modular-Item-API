package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.client.model.BakedMiapiModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.renderer.RescaledVertexConsumer;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class OverlayModelProperty extends CodecProperty<List<OverlayModelProperty.OverlayModelData>> {
    public static Codec<List<OverlayModelProperty.OverlayModelData>> CODEC = Codec.list(AutoCodec.of(OverlayModelData.class).codec());
    public static String KEY = "overlay_texture_model";
    public static OverlayModelProperty property;

    public OverlayModelProperty() {
        super(CODEC);
        property = this;
        MiapiItemModel.modelSuppliers.add((key, module, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            ModuleInstance moduleInstance = module;

            for (ModuleInstance module2 : ItemModule.getModules(stack).allSubModules()) {
                for (OverlayModelData modelData : getData(module2).orElse(new ArrayList<>())) {
                    if (!modelData.onlyOnSameModule() || moduleInstance.equals(module2)) {
                        List<ModelProperty.ModelJson> list = ModelProperty.getJson(moduleInstance);

                        list.forEach(modelJson -> {
                            if (modelData.isValid(modelJson)) {
                                ModelHolder holder = ModelProperty.bakedModel(moduleInstance, modelJson, stack, key);
                                if (holder != null) {
                                    ColorProvider colorProvider = modelData.getColorProvider(stack, module2, moduleInstance, holder.colorProvider());
                                    TextureAtlasSprite overWriteSprite = modelData.resolveSprite();
                                    models.add(getBakedMiapiModel(
                                            module,
                                            stack,
                                            modelData,
                                            moduleInstance,
                                            holder,
                                            colorProvider,
                                            overWriteSprite));
                                }
                            }
                        });
                    }
                }
            }

            return models;
        });

    }

    @NotNull
    private BakedMiapiModel getBakedMiapiModel(ModuleInstance module, ItemStack stack, OverlayModelData modelData, ModuleInstance moduleInstance, ModelHolder holder, ColorProvider colorProvider, @Nullable TextureAtlasSprite overWriteSprite) {
        return new BakedMiapiModel(
                new ModelHolder(
                        holder.model(),
                        new Matrix4f(holder.matrix4f()),
                        new ColorProvider() {
                            @Override
                            public VertexConsumer getConsumer(MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, ItemStack stack, ModuleInstance moduleInstance, ItemDisplayContext mode) {
                                return new RescaledVertexConsumer(colorProvider.getConsumer(vertexConsumers, overWriteSprite == null ? sprite : overWriteSprite, stack, modelData.useThisModule() ? module : moduleInstance, mode), sprite);
                            }

                            @Override
                            public ColorProvider getInstance(ItemStack stack, ModuleInstance instance) {
                                return this;
                            }
                        },
                        new int[]{-1, -1},
                        holder.trimMode(),
                        holder.entityRendering()
                ), modelData.useThisModule() ? module : moduleInstance, stack);
    }

    @Override
    public boolean load(ResourceLocation id, JsonElement element, boolean isClient) throws Exception {
        decode(element).forEach(OverlayModelData::loadSprite);
        return isClient;
    }

    @Override
    public List<OverlayModelData> merge(List<OverlayModelData> left, List<OverlayModelData> right, MergeType mergeType) {
        return ModuleProperty.mergeList(left, right, mergeType);
    }

    public static class OverlayModelData {
        @CodecBehavior.Optional
        public String texture;
        public String modelTargetType;
        public String modelTargetInfo;
        public String colorProvider;
        @CodecBehavior.Optional
        public double javaPriority = 0;
        @CodecBehavior.Optional
        public boolean allowOtherModules = false;

        @Nullable
        public TextureAtlasSprite resolveSprite() {
            if (texture == null) {
                return null;
            }
            return ModelProperty.textureGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(texture)));
        }

        public void loadSprite() {
            if (texture != null) {
                ModelProperty.textureGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(texture)));
            }
        }

        public ColorProvider getColorProvider(ItemStack itemStack, ModuleInstance current, ModuleInstance other, ColorProvider otherColor) {
            switch (colorProvider) {
                case "this": {
                    return ColorProvider.getProvider("material", itemStack, current);
                }
                case "other": {
                    return otherColor;
                }
                default: {
                    if (ColorProvider.colorProviders.containsKey(colorProvider)) {
                        return ColorProvider.getProvider(colorProvider, itemStack, current);
                    }
                    if (colorProvider.startsWith("material:")) {
                        String materialId = colorProvider.split(":")[1];
                        return new ColorProvider.MaterialColorProvider(MaterialProperty.materials.get(materialId));
                    }
                }
            }
            return otherColor;
        }

        public boolean useThisModule() {
            return !colorProvider.equals("other");
        }

        public boolean onlyOnSameModule() {
            return !allowOtherModules;
        }

        public boolean isValid(ModelProperty.ModelJson modelJson) {
            Pattern pattern = Pattern.compile(modelTargetInfo);
            switch (modelTargetType) {
                case "id": {
                    return pattern.matcher(modelJson.id).find();
                }
                case "path": {
                    return pattern.matcher(modelJson.path).find();
                }
                default:
                    return false;
            }
        }
    }
}

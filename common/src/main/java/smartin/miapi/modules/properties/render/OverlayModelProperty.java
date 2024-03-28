package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.BakedMiapiModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.renderer.RescaledVertexConsumer;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.modules.properties.util.CodecBasedProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class OverlayModelProperty extends CodecBasedProperty<OverlayModelProperty.OverlayModelData> implements RenderProperty {
    public static Codec<OverlayModelData> CODEC = AutoCodec.of(OverlayModelData.class).codec();
    public static String KEY = "overlay_texture_model";
    public static OverlayModelProperty property;

    public OverlayModelProperty() {
        super(KEY, CODEC);
        property = this;
        MiapiItemModel.modelSuppliers.add((key, module, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            for (OverlayModelData modelData : getData(module)) {
                for (ItemModule.ModuleInstance moduleInstance : ItemModule.getModules(stack).allSubModules()) {
                    List<ModelProperty.ModelJson> list = ModelProperty.getJson(moduleInstance);

                    list.forEach(modelJson -> {
                        if (modelData.isValid(modelJson)) {
                            ModelHolder holder = ModelProperty.bakedModel(moduleInstance, modelJson, stack, key);
                            if (holder != null) {
                                ColorProvider colorProvider = holder.colorProvider();
                                if (modelData.useThisModule) {
                                    colorProvider = ColorProvider.getProvider("material", stack, module);
                                }
                                Sprite overWriteSprite = modelData.resolveSprite();
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

            return models;
        });

    }

    public boolean load(String moduleKey, JsonElement data) {
        getData(data).forEach(OverlayModelData::loadSprite);
        return true;
    }

    @NotNull
    private BakedMiapiModel getBakedMiapiModel(ItemModule.ModuleInstance module, ItemStack stack, OverlayModelData modelData, ItemModule.ModuleInstance moduleInstance, ModelHolder holder, ColorProvider colorProvider, Sprite overWriteSprite) {
        return new BakedMiapiModel(
                new ModelHolder(
                        holder.model(),
                        holder.matrix4f(),
                        new ColorProvider() {
                            @Override
                            public VertexConsumer getConsumer(VertexConsumerProvider vertexConsumers, Sprite sprite, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode) {
                                return new RescaledVertexConsumer(colorProvider.getConsumer(vertexConsumers, overWriteSprite, stack, modelData.useThisModule ? module : moduleInstance, mode), sprite);
                            }

                            @Override
                            public ColorProvider getInstance(ItemStack stack, ItemModule.ModuleInstance instance) {
                                return this;
                            }
                        },
                        new int[]{-1, -1},
                        holder.trimMode(),
                        holder.entityRendering()
                ), modelData.useThisModule ? module : moduleInstance, stack);
    }

    public static List<OverlayModelData> getData(ItemModule.ModuleInstance moduleInstance) {
        JsonElement element = moduleInstance.getProperties().get(property);
        return getData(element);
    }

    public static List<OverlayModelData> getData(JsonElement element) {
        List<OverlayModelData> data = new ArrayList<>();
        if (element != null && element.isJsonArray()) {
            element.getAsJsonArray().forEach(element1 -> {
                try {
                    data.add(CODEC.parse(JsonOps.INSTANCE, element1).getOrThrow(false, s ->
                            Miapi.LOGGER.error("Failed to load OverlayModelData! -> {}", s)));
                } catch (Exception e) {
                }
            });
        }
        return data;
    }

    public static class OverlayModelData {
        public String texture;
        public String modelTargetType;
        public String modelTargetInfo;
        public boolean useThisModule = true;

        public Sprite resolveSprite() {
            return ModelProperty.textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(texture)));
        }

        public void loadSprite() {
            ModelProperty.textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(texture)));
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

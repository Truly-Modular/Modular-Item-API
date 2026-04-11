package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.client.model.item.BakedSingleModel;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.ColorController;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.baked.ModelData;
import smartin.miapi.modules.properties.render.baked.ModelProperty;
import smartin.miapi.modules.properties.render.baked.UnbakedModelHolder;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * this class is meant to be used with {@link smartin.miapi.client.model.module.BakedMiapiModel}
 * @param model the model to be rendered
 * @param matrix4f the models local projection matrix
 * @param colorProvider the colorprovider to recolor the model at runtime
 * @param lightValues lighting overwrites, only used if its brighter
 * @param trimMode what trims apply to the model
 * @param entityRendering if true both sides of the quads are rendered, set to true for armor, false for handhelds
 * @param renderBanner if a banner should be overlayed to the model (experimental)
 */
@Environment(EnvType.CLIENT)
public record ModelHolder(BakedModel model, Matrix4f matrix4f, ColorProvider colorProvider,
                          int[] lightValues, TrimRenderer.TrimMode trimMode, boolean entityRendering,
                          BannerMode renderBanner) {
    public ModelHolder(BakedModel model, Matrix4f matrix4f, ColorProvider colorProvider,
                       int[] lightValues, TrimRenderer.TrimMode trimMode, boolean entityRendering) {
        this(model, matrix4f, colorProvider, lightValues, trimMode, entityRendering, BannerMode.NONE);
    }

    @Nullable
    public static ModelHolder bakedModel(ModuleInstance instance, ModelData json, ItemStack itemStack, String key) {
        int condition = ColorController.getColor(StatResolver.resolveString(json.condition, instance));
        if (condition != 0) {
            if (
                    json.transform.origin == null && "item".equals(key) ||
                    json.transform.origin != null && json.transform.origin.equals(key) ||
                    ("item".equals(json.transform.origin) && key == null)) {
                return bakedModel(instance, json, itemStack);
            }
        }
        return null;
    }

    @Nullable
    public static ModelHolder bakedModel(ModuleInstance instance, ModelData json, ItemStack itemStack) {
        Material material = MaterialProperty.getMaterial(instance);
        json.repair();
        List<String> list = new ArrayList<>();
        if (material != null) {
            list.add(material.getStringID());
            list = material.getTextureKeys();
        } else {
            list.add("default");
        }
        UnbakedModelHolder unbakedModel = null;
        for (String str : list) {
            String fullPath = json.path.replace("[material.texture]", str);
            if (ModelProperty.modelCache.containsKey(fullPath)) {
                unbakedModel = ModelProperty.modelCache.get(fullPath);
                break;
            }
        }
        if (unbakedModel == null) {
            String fullPath = json.path.replace("[material.texture]", "default");
            if (ModelProperty.modelCache.containsKey(fullPath)) {
                unbakedModel = ModelProperty.modelCache.get(fullPath);
            } else {
                return null;
            }
        }
        BakedSingleModel model = DynamicBakery.bakeModel(unbakedModel.model(), ModelProperty.textureGetter, FastColor.ARGB32.color(255, 255, 255, 255), Transform.IDENTITY);
        if(model==null){
            return null;
        }
        Matrix4f matrix4f = Transform.toModelTransformation(json.transform).toMatrix();
        String colorProviderId = unbakedModel.modelMetadata().colorProvider() != null ?
                unbakedModel.modelMetadata().colorProvider() : json.color_provider;
        ColorProvider colorProvider = ColorProvider.getProvider(colorProviderId, itemStack, instance, json.getTrimMode());
        if (colorProvider == null) {
            throw new RuntimeException("colorProvider is null");
        }
        return new ModelHolder(
                model.optimize(), matrix4f, colorProvider,
                unbakedModel.modelMetadata().lightValues() == null ? new int[]{-1, -1} : unbakedModel.modelMetadata().lightValues(),
                json.getTrimMode(), json.entity_render);
    }

    public enum BannerMode {
        NONE,
        BASE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelHolder other)) return false;
        return entityRendering == other.entityRendering
               && Objects.equals(model, other.model)
               && Objects.equals(matrix4f, other.matrix4f)
               && Objects.equals(colorProvider, other.colorProvider)
               && Arrays.equals(lightValues, other.lightValues)
               && trimMode == other.trimMode
               && renderBanner == other.renderBanner;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(model, matrix4f, colorProvider, trimMode, entityRendering, renderBanner);
        result = 31 * result + Arrays.hashCode(lightValues);
        return result;
    }
}

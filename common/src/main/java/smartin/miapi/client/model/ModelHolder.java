package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import org.joml.Matrix4f;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

import java.util.Arrays;
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

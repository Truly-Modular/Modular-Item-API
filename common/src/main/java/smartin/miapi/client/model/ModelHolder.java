package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import org.joml.Matrix4f;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

import java.util.Arrays;
import java.util.Objects;

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

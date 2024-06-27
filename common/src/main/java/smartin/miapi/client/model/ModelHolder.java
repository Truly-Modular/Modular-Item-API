package smartin.miapi.client.model;

import net.minecraft.client.resources.model.BakedModel;
import org.joml.Matrix4f;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

public record ModelHolder(BakedModel model, Matrix4f matrix4f, ColorProvider colorProvider,
                          int[] lightValues, TrimRenderer.TrimMode trimMode, boolean entityRendering) {

}

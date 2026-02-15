package smartin.miapi.client.model.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix4f;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class GeckoLibMiapiModel implements MiapiModel {
    public GeoModel geoModel;
    public String attachedBone = "bone";
    public boolean attachSubmodelToBone = false;
    public GeoRenderer renderer;
    ModelHolder modelHolder;

    public GeckoLibMiapiModel() {
    }

    @Override
    public void render(RenderContext context) {


        //VertexConsumer vertexConsumer = modelHolder.colorProvider().getConsumer(vertexConsumers, quad.getSprite(), stack, instance, transformationMode);

        renderer.defaultRender(context.matrices(), null, context.vertexConsumers(), null, null, 0, context.tickDelta(), context.light());
    }

    public Matrix4f subModuleMatrix(RenderContext context) {
        if (attachSubmodelToBone) {
            Optional<GeoBone> bone = geoModel.getBone(attachedBone);
            return bone.get().getLocalSpaceMatrix();
        }
        return new Matrix4f();
    }
}

package smartin.miapi.client.model;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

import java.util.Optional;

public class GeckoLibMiapiModel implements MiapiModel {
    public GeoModel geoModel;
    public String attachedBone = "bone";
    public boolean attachSubmodelToBone = false;
    public GeoRenderer renderer;
    ModelHolder modelHolder;

    public GeckoLibMiapiModel() {
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {


        //VertexConsumer vertexConsumer = modelHolder.colorProvider().getConsumer(vertexConsumers, quad.getSprite(), stack, instance, transformationMode);

        renderer.defaultRender(matrices, null, vertexConsumers, null, null, 0, tickDelta, light);
    }

    public Matrix4f subModuleMatrix() {
        if (attachSubmodelToBone) {
            Optional<GeoBone> bone = geoModel.getBone(attachedBone);
            return bone.get().getLocalSpaceMatrix();
        }
        return new Matrix4f();
    }
}

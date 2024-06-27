package smartin.miapi.client.model;

import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GeckoLibMiapiModel implements MiapiModel {
    public GeoModel geoModel;
    public String attachedBone = "bone";
    public boolean attachSubmodelToBone = false;
    public GeoRenderer renderer;
    ModelHolder modelHolder;

    public GeckoLibMiapiModel() {
    }

    @Override
    public void render(PoseStack matrices, ItemStack stack, ItemDisplayContext transformationMode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {


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

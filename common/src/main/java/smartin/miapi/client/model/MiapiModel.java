package smartin.miapi.client.model;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

/**
 * default model to implement Module level Models.
 * Should be registered at {@link MiapiItemModel#modelSuppliers}
 */
public interface MiapiModel {
    void render(MatrixStack matrices,
                ItemStack stack,
                ModelTransformationMode transformationMode,
                float tickDelta,
                VertexConsumerProvider vertexConsumers,
                LivingEntity entity,
                int light,
                int overlay);

    default Matrix4f subModuleMatrix() {
        return new Matrix4f();
    }
}

package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * default model to implement Module level Models.
 * Should be registered at {@link MiapiItemModel#modelSuppliers}
 */
public interface MiapiModel {

    void render(PoseStack matrices,
                ItemStack stack,
                ItemDisplayContext transformationMode,
                float tickDelta,
                MultiBufferSource vertexConsumers,
                LivingEntity entity,
                int light,
                int overlay);

    default Matrix4f subModuleMatrix() {
        return new Matrix4f();
    }
}

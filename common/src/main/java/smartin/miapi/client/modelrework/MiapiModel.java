package smartin.miapi.client.modelrework;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public interface MiapiModel {
    void render(MatrixStack matrices, ItemStack stack, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay);

    @Nullable Matrix4f subModuleMatrix(int submoduleId);
}

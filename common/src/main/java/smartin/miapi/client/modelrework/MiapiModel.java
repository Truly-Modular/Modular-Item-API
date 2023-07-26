package smartin.miapi.client.modelrework;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public interface MiapiModel {
    void render(MatrixStack matrices, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay);
}

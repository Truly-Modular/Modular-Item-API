package smartin.miapi.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.material.MaterialIcons;

public class BlockRenderModel implements MiapiModel {
    BlockState blockState;
    public MaterialIcons.SpinSettings spinSettings = null;
    Transform transform;

    public BlockRenderModel(BlockState block, Transform transform) {
        blockState = block;
        this.transform = transform;
    }

    @Override
    public void render(MatrixStack matrixStack, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {

        matrixStack.push();

        transform.applyPosition(matrixStack);

        if (spinSettings != null) {
            spinSettings.multiplyMatrices(matrixStack);
        }

        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                blockState,
                matrixStack,
                vertexConsumers,
                light,
                overlay);

        matrixStack.pop();
    }
}

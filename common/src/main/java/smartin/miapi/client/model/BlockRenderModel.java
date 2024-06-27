package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
    public void render(PoseStack matrixStack, ItemStack stack, ItemDisplayContext transformationMode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {

        matrixStack.pushPose();

        transform.applyPosition(matrixStack);

        if (spinSettings != null) {
            spinSettings.multiplyMatrices(matrixStack);
        }

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockState,
                matrixStack,
                vertexConsumers,
                light,
                overlay);

        matrixStack.popPose();
    }
}

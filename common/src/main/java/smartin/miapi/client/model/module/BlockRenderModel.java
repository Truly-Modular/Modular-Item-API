package smartin.miapi.client.model.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialIcons;

@Environment(EnvType.CLIENT)
public class BlockRenderModel implements MiapiModel {
    BlockState blockState;
    public MaterialIcons.SpinSettings spinSettings = null;
    Transform transform;

    public BlockRenderModel(BlockState block, Transform transform) {
        blockState = block;
        this.transform = transform;
    }

    @Override
    public void render(RenderContext context) {

        context.matrices().pushPose();

        transform.applyPosition(context.matrices());

        if (spinSettings != null) {
            spinSettings.multiplyMatrices(context.matrices());
        }

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockState,
                context.matrices(),
                context.vertexConsumers(),
                context.light(),
                context.overlay());

        context.matrices().popPose();
    }
}

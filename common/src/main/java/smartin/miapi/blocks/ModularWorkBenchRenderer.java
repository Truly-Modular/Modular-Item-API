package smartin.miapi.blocks;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.math.RotationAxis;
import smartin.miapi.item.modular.VisualModularItem;

public class ModularWorkBenchRenderer implements BlockEntityRenderer<ModularWorkBenchEntity> {
    private final BlockEntityRendererFactory.Context context;

    public ModularWorkBenchRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(ModularWorkBenchEntity be, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = be.getItem();
        if (stack.isEmpty()) return;

        matrices.push();
        matrices.translate(8 / 16f, 16.5f / 16, 8 / 16f);
        float rotAmnt = be.getCachedState().get(ModularWorkBench.FACING).asRotation();
        if (!(stack.getItem() instanceof Equipment) && (
                stack.getItem() instanceof VisualModularItem ||
                        stack.getItem() instanceof ToolItem ||
                        stack.getItem() instanceof SwordItem ||
                        stack.getItem() instanceof ArrowItem ||
                        stack.getItem() instanceof CrossbowItem ||
                        stack.getItem() instanceof RangedWeaponItem))
            rotAmnt -= 45;
        else
            rotAmnt -= 90;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotAmnt));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.scale(0.75f, 0.75f, 0.75f);

        try {
            context.getItemRenderer().renderItem(
                    stack,
                    ModelTransformationMode.FIXED,
                    light, overlay,
                    matrices, vertexConsumers,
                    be.getWorld(), 1
            );
        } catch (Exception ignored) {
        }
        matrices.pop();
    }
}

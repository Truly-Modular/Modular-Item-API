package smartin.miapi.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.item.*;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import smartin.miapi.item.modular.VisualModularItem;

public class ModularWorkBenchRenderer implements BlockEntityRenderer<ModularWorkBenchEntity> {
    private final BlockEntityRendererProvider.Context context;

    public ModularWorkBenchRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(ModularWorkBenchEntity be, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        ItemStack stack = be.getItem();
        if (stack.isEmpty()) return;

        matrices.pushPose();
        matrices.translate(8 / 16f, 16.5f / 16, 8 / 16f);
        float rotAmnt = be.getBlockState().getValue(ModularWorkBench.FACING).toYRot();
        if (!(stack.getItem() instanceof Equipable) && (
                stack.getItem() instanceof VisualModularItem ||
                        stack.getItem() instanceof TieredItem ||
                        stack.getItem() instanceof SwordItem ||
                        stack.getItem() instanceof ArrowItem ||
                        //stack.getItem() instanceof CrossbowItem ||
                        stack.getItem() instanceof ProjectileWeaponItem))
            rotAmnt -= 45;
        else
            rotAmnt -= 90;
        matrices.mulPose(Axis.YP.rotationDegrees(rotAmnt));
        matrices.mulPose(Axis.XP.rotationDegrees(90));
        matrices.scale(0.75f, 0.75f, 0.75f);

        try {
            context.getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    light, overlay,
                    matrices, vertexConsumers,
                    be.getLevel(), 1
            );
        } catch (Exception ignored) {
        }
        matrices.popPose();
    }
}

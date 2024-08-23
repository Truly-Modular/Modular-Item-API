package smartin.miapi.client.model;

import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemInModuleMiapiModel implements MiapiModel {

    final Supplier<ItemStack> stackSupplier;
    final Matrix4f matrix4f;

    public ItemInModuleMiapiModel(Supplier<ItemStack> stack, Matrix4f matrix4f){
        this.stackSupplier = stack;
        this.matrix4f = matrix4f;
    }

    @Override
    public void render(PoseStack matrices, ItemStack stack, ItemDisplayContext transformationMode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {
        Minecraft.getInstance().level.getProfiler().push("ItemOnTopRendering");
        matrices.pushPose();
        matrices.mulPose(matrix4f);
        ItemStack modelStack = stackSupplier.get();
        if(modelStack.getItem() instanceof FireworkRocketItem){
            matrices.mulPose(Axis.ZP.rotationDegrees(45));
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(
                modelStack,
                ItemDisplayContext.FIXED,
                light,
                overlay,
                matrices,
                vertexConsumers,
                Minecraft.getInstance().level,
                0);
        matrices.popPose();
        Minecraft.getInstance().level.getProfiler().pop();
    }
}

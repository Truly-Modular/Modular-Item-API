package smartin.miapi.client.model.module;

import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import smartin.miapi.client.model.MiapiModel;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ItemInModuleMiapiModel implements MiapiModel {

    final Supplier<ItemStack> stackSupplier;
    final Matrix4f matrix4f;

    public ItemInModuleMiapiModel(Supplier<ItemStack> stack, Matrix4f matrix4f){
        this.stackSupplier = stack;
        this.matrix4f = matrix4f;
    }

    @Override
    public void render(RenderContext context) {
        Minecraft.getInstance().getProfiler().push("ItemOnTopRendering");
        context.matrices().pushPose();
        context.matrices().mulPose(matrix4f);
        ItemStack modelStack = stackSupplier.get();
        if(modelStack.getItem() instanceof FireworkRocketItem){
            context.matrices().mulPose(Axis.ZP.rotationDegrees(45));
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(
                modelStack,
                ItemDisplayContext.FIXED,
                context.light(),
                context.overlay(),
                context.matrices(),
                context.vertexConsumers(),
                Minecraft.getInstance().level,
                0);
        context.matrices().popPose();
        Minecraft.getInstance().getProfiler().pop();
    }
}
